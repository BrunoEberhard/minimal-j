package ch.openech.mj.vaadin;

import ch.openech.mj.page.Page;

public interface UriFragmentPageFactory {

	public String getFragmentFor(Page page);
	
	public Page getPageFor(String fragment);
	
}
