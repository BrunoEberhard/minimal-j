package org.minimalj.frontend.page;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;

public class EmptyPage extends AbstractPage {

	public EmptyPage(PageContext context) {
		super(context);
	}
	
	@Override
	public IContent getContent() {
		return null;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}

}
