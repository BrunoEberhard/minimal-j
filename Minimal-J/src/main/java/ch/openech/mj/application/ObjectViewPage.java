package ch.openech.mj.application;

import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;

public abstract class ObjectViewPage<T> extends Page implements RefreshablePage {

	private FormVisual<T> objectPanel;
	private IComponent alignLayout;
	
	public ObjectViewPage(PageContext context) {
		super(context);
	}

	protected abstract T loadObject();

	protected abstract FormVisual<T> createForm();
	
	@Override
	public IComponent getPanel() {
		if (alignLayout == null) {
			objectPanel = createForm();
			alignLayout = ClientToolkit.getToolkit().createFormAlignLayout(objectPanel);
			refresh();
		}
		return alignLayout;
	}
	
	protected void showObject(T object) {
		objectPanel.setObject(object);
	}
	
	@Override
	public void refresh() {
		showObject(loadObject());

	}
	
}
