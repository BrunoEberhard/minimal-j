package ch.openech.mj.application;

import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.IComponent;

public class EmptyPage extends Page {

	public EmptyPage(PageContext context) {
		super(context);
	}
	
	@Override
	public IComponent getPanel() {
		return null;
	}

}
