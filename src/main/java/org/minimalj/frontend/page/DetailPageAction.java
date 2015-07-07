package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class DetailPageAction extends Action {

	private final Page detailPage;
	
	public DetailPageAction(Page detailPage) {
		this.detailPage = detailPage;
	}
	
	@Override
	public void action() {
		ClientToolkit.getToolkit().showDetail(detailPage);
	}
	
}
