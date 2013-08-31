package ch.openech.mj.page;

import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;

public abstract class ObjectViewPage<T> extends AbstractPage {

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
	
}
