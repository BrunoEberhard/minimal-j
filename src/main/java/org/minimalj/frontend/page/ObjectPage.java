package org.minimalj.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.util.IdUtils;

public abstract class ObjectPage<T> implements Page {

	private final Class<T> objectClass;
	private final Object objectId;
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

	protected T getObject() {
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
		form.setObject(getObject());
		return form.getContent();
	}

	public T load() {
		return Backend.getInstance().read(objectClass, objectId);
	}
	
	public void unload() {
		object = null;
	}

}
