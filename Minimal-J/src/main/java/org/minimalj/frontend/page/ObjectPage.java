package org.minimalj.frontend.page;

import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;

public abstract class ObjectPage<T> extends AbstractPage {

	private Form<T> form;
	
	public ObjectPage() {
	}

	public ActionGroup getMenu() {
		return null;
	}

	public void updateObject(T object) {
		form.setObject(object);
	}
	
	protected abstract Form<T> createForm();

	protected abstract T getObject();
	
	@Override
	public IContent getContent() {
		if (form == null) {
			form = createForm();
			refresh();
		}
		return form.getContent();
	}
	
	@Override
	public void refresh() {
		form.setObject(getObject());
	}
	
}
