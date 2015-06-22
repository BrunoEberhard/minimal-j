package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class DetailPageAction extends Action {

	private final Page page;
	private final PageWithDetail pageWithDetail;
	
	public DetailPageAction(Page page, PageWithDetail pageWithDetail) {
		this.page = page;
		this.pageWithDetail = pageWithDetail;
	}
	
	@Override
	public void action() {
		ClientToolkit.getToolkit().show(page, pageWithDetail);
	}
	
}
