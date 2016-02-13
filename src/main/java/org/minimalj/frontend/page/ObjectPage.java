package org.minimalj.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.IdUtils;

/**
 * This page requires the object to be persistet. Meaning the object has to have
 * its id. If you want to display an object without involving a backend use a
 * normal page with this pattern:
 * 
 * <pre>
 * public class TeaCup extends Page {
 * 
 * 	private final TaxStatement teaCup;
 * 
 * 	public TeaCup(TaxStatement teaCup) {
 * 		this.teaCup = teaCup;
 * 	}
 * 
 * 	public IContent getContent() {
 * 		TaxStatementForm form = new TaxStatementForm(Form.READ_ONLY);
 * 		form.setObject(teaCup);
 * 		return form.getContent();
 * 	}
 * }
 * </pre>
 *
 * But be carefull as this pattern keeps the complete object in the memory.
 */
public abstract class ObjectPage<T> extends Page {

	private final Class<T> objectClass;
	private Object objectId;
	private transient T object;
	private transient Form<T> form;
	
	@SuppressWarnings("unchecked")
	public ObjectPage(T object) {
		this((Class<T>) object.getClass(), IdUtils.getId(object, !IdUtils.PLAIN));
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
			objectId = IdUtils.getId(object, IdUtils.PLAIN);
			this.object = object;
			if (form != null) {
				form.setObject(object);
			}
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
		if (object == null) {
			object = load();
		}
		return object;
	}
	
	@Override
	public IContent getContent() {
		if (form == null) {
			form = createForm();
		}
		unload();
		// TODO try catch around getObject to catch load problems and display stack trace
		form.setObject(getObject());
		return form.getContent();
	}

	public T load() {
		return Backend.read(objectClass, objectId);
	}
	
	public void unload() {
		object = null;
	}
	
	public void refresh() {
		setObject(load());
	}
	
	public abstract class ObjectEditor extends Editor<T, T> {
		
		@Override
		protected T createObject() {
			return CloneHelper.clone(ObjectPage.this.getObject());
		}
		
		@Override
		protected void finished(T result) {
			setObject(result);
		}
	}

}
