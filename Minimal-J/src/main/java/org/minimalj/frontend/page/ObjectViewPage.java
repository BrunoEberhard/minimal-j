package org.minimalj.frontend.page;

import org.minimalj.frontend.edit.form.IForm;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IComponent;

public abstract class ObjectViewPage<T> extends AbstractPage implements RefreshablePage {

	private IForm<T> objectPanel;
	private IComponent alignLayout;
	
	public ObjectViewPage(PageContext pageContext) {
		super(pageContext);
	}

	protected abstract IForm<T> createForm();

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
