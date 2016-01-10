package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;

public class DetailPageAction extends Action {

	private final Page mainPage;
	private final Page detailPage;

	public DetailPageAction(Page mainPage, Page detailPage) {
		this(mainPage, detailPage, detailPage.getTitle());
	}
	
	public DetailPageAction(Page mainPage, Page detailPage, String name) {
		super(name);
		this.mainPage = mainPage;
		this.detailPage = detailPage;
	}
	
	@Override
	public void action() {
		Frontend.showDetail(mainPage, detailPage);
	}
	
}
