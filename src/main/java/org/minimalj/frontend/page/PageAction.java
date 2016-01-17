package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;

public class PageAction extends Action {

	private final Page page;
	
	public PageAction(Page page) {
		this(page, null);
	}
	
	public PageAction(Page page, String name) {
		super(name != null ? name : page.getTitle());
		this.page = page;
	}

	@Override
	public void action() {
		Frontend.show(page);
	}
	
}
