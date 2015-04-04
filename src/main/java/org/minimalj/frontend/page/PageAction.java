package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;

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
		ClientToolkit.getToolkit().show(page);
	}
	
}
