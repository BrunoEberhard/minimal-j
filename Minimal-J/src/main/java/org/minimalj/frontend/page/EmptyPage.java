package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.IComponent;

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
