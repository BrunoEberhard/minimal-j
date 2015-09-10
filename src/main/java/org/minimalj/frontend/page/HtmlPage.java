package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;

public class HtmlPage extends Page {

	private final String htmlOrUrl;
	
	public HtmlPage(String htmlOrUrl) {
		this.htmlOrUrl = htmlOrUrl;
	}
	
	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent(htmlOrUrl);
	}

}
