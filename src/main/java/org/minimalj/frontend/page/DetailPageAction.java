package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;

public final class DetailPageAction extends Action {

	private final Page mainPage, detail;
	
	public DetailPageAction(Page mainPage, Page detail) {
		this(mainPage, detail, null);
	}
	
	public DetailPageAction(Page mainPage, Page detail, String name) {
		super(name != null ? name : detail.getTitle());
		this.mainPage = mainPage;
		this.detail = detail;
	}

	@Override
	public void run() {
		Frontend.showDetail(mainPage, detail);
	}
	
}