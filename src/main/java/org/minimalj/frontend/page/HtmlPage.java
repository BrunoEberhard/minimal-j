package org.minimalj.frontend.page;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;

/**
 * <b>note:</b> for security reasons read the JavaDoc in the Frontend class
 * 
 * @see Frontend#createHtmlContent(String)
 * @see StringUtils#sanitizeHtml(String)
 */
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
