package ch.openech.mj.page;

import ch.openech.mj.toolkit.IComponent;

public class EmptyPage implements Page {

	public EmptyPage(PageContext context) {
	}
	
	@Override
	public String getTitle() {
		return "";
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
