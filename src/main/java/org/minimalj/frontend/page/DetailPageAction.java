package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;

public class DetailPageAction extends Action {

	private final Page detailPage;
	
	public DetailPageAction(Page detailPage) {
		this.detailPage = detailPage;
	}
	
	@Override
	public void action() {
		Frontend.getBrowser().showDetail(detailPage);
	}
	
}
