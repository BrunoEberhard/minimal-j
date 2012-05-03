package ch.openech.mj.page;

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
