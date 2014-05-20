package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.IComponent;


public interface Page {
	
	public String getTitle();
	
	public IComponent getComponent();

	public ActionGroup getMenu();
	
}