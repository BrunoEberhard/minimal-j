package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;

public class PageAction extends Action {

	private final Page page;
	private final String name;
	
	public PageAction(Page page) {
		this(page, null);
	}
	
	public PageAction(Page page, String name) {
		this.page = page;
		this.name = name != null ? name : page.getTitle();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void action() {
		Frontend.getBrowser().show(page);
	}
	
}
