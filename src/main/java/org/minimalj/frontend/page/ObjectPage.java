package org.minimalj.frontend.page;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.TableDetailPage.ChangeableDetailPage;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.resources.Resources;

/**
 * This page requires the object to be persistent. Meaning the object has to have
 * its id. If you want to display an object without involving a Backend use a
 * normal page with this pattern:
 * 
 * <pre>
 * public class TeaCupPage implements Page {
 * 
 * 	private final TeaCup teaCup;
 * 
 * 	public TeaCup(TeaCup teaCup) {
 * 		this.teaCup = teaCup;
 * 	}
 * 
 * 	public IContent getContent() {
 * 		TeaCupForm form = new TeaCupForm(Form.READ_ONLY);
 * 		form.setObject(teaCup);
 * 		return form.getContent();
 * 	}
 * }
 * </pre>
 *
 * But be careful as this pattern keeps the complete object in the memory.
 */
public abstract class ObjectPage<T> implements ChangeableDetailPage<T> {

	private final Class<T> objectClass;
	private Object objectId;
	private SoftReference<T> object;
	private SoftReference<Form<T>> form;
	private long lastLoad = Long.MIN_VALUE;
	
	@SuppressWarnings("unchecked")
	public ObjectPage(T object) {
		this((Class<T>) object.getClass(), IdUtils.getId(object));
	}
	
	public ObjectPage(Class<T> objectClass, Object objectId) {
		this.objectClass = objectClass;
		this.objectId = objectId;
	}

	public void setObject(T object) {
		if (object == null) {
			throw new NullPointerException();
		} else if (object.getClass() != objectClass) {
			throw new IllegalArgumentException("Object is " + object.getClass() + " instead of " + objectClass);
		} else {
			objectId = IdUtils.getId(object);
			this.object = new SoftReference<>(object);
			Form<T> form = this.form != null ? this.form.get() : null;
			if (form != null) {
				form.setObject(object);
			}
		}
		if (editors != null) {
			editors.forEach(c -> c.setObject(object));
		}
	}
	
	@Override
	public String getTitle() {
		String title = Resources.getStringOrNull(getClass());
		if (title != null) {
			return title;
		} else {
			Class<?> clazz = GenericUtils.getGenericClass(getClass());
			return Resources.getString(clazz);
		}
	}
	
	protected abstract Form<T> createForm();

	public Class<T> getObjectClass() {
		return objectClass;
	}

	public Object getObjectId() {
		return objectId;
	}

	public T getObject() {
		T object = this.object != null ? this.object.get() : null;
		if (object == null || lastLoad < System.currentTimeMillis() - 30000) {
			object = load();
			lastLoad = System.currentTimeMillis();
			this.object = new SoftReference<>(object);
		}
		return object;
	}
	
	@Override
	public IContent getContent() {
		Form<T> form = this.form != null ? this.form.get() : null;
		if (form == null) {
			form = createForm();
			this.form = new SoftReference<>(form);
		}
		// TODO try catch around getObject to catch load problems and display stack trace
		form.setObject(getObject());
		return form.getContent();
	}

	public T load() {
		return Backend.read(objectClass, objectId);
	}
	
	public void refresh() {
		setObject(load());
		lastLoad = System.currentTimeMillis();
	}
	
	public abstract class ObjectEditor extends Editor<T, T> {
		
		@Override
		protected T createObject() {
			return CloneHelper.clone(ObjectPage.this.getObject());
		}
		
		@Override
		protected Class<?> getEditedClass() {
			return GenericUtils.getGenericClass(ObjectPage.this.getClass());
		}
		
		@Override
		protected void finished(T result) {
			setObject(result);
		}
	}

	private List<ObjectPageEditor<?>> editors;

	// experimental
	public abstract class ObjectPageEditor<EDIT> extends Editor<EDIT, T> {
		protected T object;

		public ObjectPageEditor() {
			if (editors == null) {
				editors = new ArrayList<>();
			}
			editors.add(this);
		}

		public void setObject(T object) {
			this.object = object;
		}

		@Override
		protected void finished(T result) {
			ObjectPage.this.setObject(result);
		}
	}

	@Override
	public void setObjects(List<T> objects) {
		if (!objects.isEmpty()) {
			setObject(objects.get(0));
		} else {
			setObject(null);
		}
	}

}
