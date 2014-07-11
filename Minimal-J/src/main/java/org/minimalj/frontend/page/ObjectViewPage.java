package org.minimalj.frontend.page;

import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IComponent;

public abstract class ObjectViewPage<T> extends AbstractPage implements RefreshablePage {

	private Form<T> objectPanel;
	private IComponent alignLayout;
	
	public ObjectViewPage(PageContext pageContext) {
		super(pageContext);
	}

	protected abstract Form<T> createForm();

	protected abstract T getObject();
	
	@Override
	public IComponent getComponent() {
		if (alignLayout == null) {
			objectPanel = createForm();
			objectPanel.setObject(getObject());
			alignLayout = ClientToolkit.getToolkit().createFormAlignLayout(objectPanel.getComponent());
		}
		return alignLayout;
	}
	
	@Override
	public void refresh() {
		objectPanel.setObject(getObject());
	}
	
}
