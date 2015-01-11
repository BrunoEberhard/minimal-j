package org.minimalj.frontend.page;

import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;

public abstract class ObjectPage<T> extends AbstractPage {

	private Form<T> form;
	private T object;
	
	public ObjectPage() {
	}

	public ActionGroup getMenu() {
		return null;
	}

	protected abstract Form<T> createForm();

	protected abstract T loadObject();
	
	protected T getObject() {
		if (object == null) {
			object = loadObject();
		}
		return object;
	}
	
	@Override
	public IContent getContent() {
		if (form == null) {
			form = createForm();
			form.setObject(getObject());
		}
		return form.getContent();
	}
	
	@Override
	public void refresh() {
		object = null;
		form.setObject(getObject());
	}
	
}
