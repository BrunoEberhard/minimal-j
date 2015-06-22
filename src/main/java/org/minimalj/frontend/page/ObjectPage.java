package org.minimalj.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.IdUtils;

public abstract class ObjectPage<T> implements Page {

	private final Class<T> objectClass;
	private Object objectId;
	private transient T object;
	private transient Form<T> form;
	
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
			this.object = object;
			if (form != null) {
				form.setObject(object);
			}
		}
	}
	
	public ActionGroup getMenu() {
		return null;
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
		return Backend.getInstance().read(objectClass, objectId);
	}
	
	public void unload() {
		object = null;
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
