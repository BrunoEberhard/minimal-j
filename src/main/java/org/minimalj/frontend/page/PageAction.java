package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Routing.Routable;

public final class PageAction extends Action implements Routable {

	private final Page page;
	
	public PageAction(Page page) {
		this(page, null);
	}
	
	public PageAction(Page page, String name) {
		super(name != null ? name : page.getTitle());
		this.page = page;
	}

	@Override
	public void run() {
		Frontend.show(page);
	}
	
	public Page getPage() {
		return page;
	}
	
	@Override
	public String getRoute() {
		return Routing.getRouteSafe(page);
	}
}
