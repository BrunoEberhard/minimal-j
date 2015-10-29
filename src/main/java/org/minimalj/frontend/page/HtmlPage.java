package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;

public class HtmlPage extends Page {

	private final String htmlOrUrl;
	private final String title;
	
	public HtmlPage(String htmlOrUrl, String title) {
		this.htmlOrUrl = htmlOrUrl;
		this.title = title;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent(htmlOrUrl);
	}
}
