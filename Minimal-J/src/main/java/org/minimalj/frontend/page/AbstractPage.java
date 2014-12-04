package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.util.resources.Resources;


public abstract class AbstractPage implements Page {

	protected AbstractPage() {
	}

	@Override
	public String getTitle() {
		return Resources.getString(getClass());
	}

	protected void show(Class<? extends Page> pageClass, String... args) {
		String pageLink = PageLink.link(pageClass, args);
		ClientToolkit.getToolkit().show(pageLink);
	}

}
