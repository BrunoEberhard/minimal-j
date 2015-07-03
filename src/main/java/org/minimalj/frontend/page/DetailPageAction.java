package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class DetailPageAction extends Action {

	private final Page detailPage;
	private final PageWithDetail parent;
	
	public DetailPageAction(PageWithDetail parent, Page detailPage) {
		this.detailPage = detailPage;
		this.parent = parent;
	}
	
	@Override
	public void action() {
		ClientToolkit.getToolkit().show(parent, detailPage);
	}
	
}
