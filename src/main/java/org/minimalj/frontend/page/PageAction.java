package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class PageAction extends Action {

	private final Page page;
	private final String name;
	private final boolean topPage;
	
	public PageAction(Page page) {
		this(page, null, true);
	}
	
	public PageAction(Page page, String name) {
		this(page, name, true);
	}

	public PageAction(Page page, String name, boolean topPage) {
		this.page = page;
		this.name = name != null ? name : page.getTitle();
		this.topPage = topPage;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void action() {
		ClientToolkit.getToolkit().show(page, topPage);
	}
	
}
