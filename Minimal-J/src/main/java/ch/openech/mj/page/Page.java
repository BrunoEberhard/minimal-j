package ch.openech.mj.page;

import ch.openech.mj.toolkit.IComponent;


public interface Page {
	
	public String getTitle();
	
	public IComponent getComponent();

	public ActionGroup getMenu();
	
}