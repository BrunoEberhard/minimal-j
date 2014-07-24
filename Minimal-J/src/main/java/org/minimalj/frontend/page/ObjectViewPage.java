package org.minimalj.frontend.page;

import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;

public abstract class ObjectViewPage<T> extends AbstractPage implements RefreshablePage {

	private Form<T> objectPanel;
	
	public ObjectViewPage() {
	}

	protected abstract Form<T> createForm();

	protected abstract T getObject();
	
	@Override
	public IContent getContent() {
		if (objectPanel == null) {
			objectPanel = createForm();
			refresh();
		}
		return objectPanel.getContent();
	}
	
	@Override
	public void refresh() {
		objectPanel.setObject(getObject());
	}
	
}
