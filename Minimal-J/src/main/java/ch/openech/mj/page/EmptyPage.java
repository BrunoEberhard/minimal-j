package ch.openech.mj.page;

import ch.openech.mj.toolkit.IComponent;

public class EmptyPage extends AbstractPage {

	public EmptyPage(PageContext context) {
		super(context);
	}
	
	@Override
	public IComponent getComponent() {
		return null;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}

}
