package org.minimalj.frontend.page;

import java.util.Objects;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;

/**
 * <b>note:</b> for security reasons read the JavaDoc in the Frontend class
 * 
 * @see Frontend#createHtmlContent(String)
 * @see StringUtils#escapeHTML(String)
 */
public class HtmlPage extends Page {

	private final String html;
	private final String title;

	public HtmlPage(String html) {
		this(html, null);
	}

	public HtmlPage(String html, String title) {
		this.html = Objects.requireNonNull(html);
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title != null ? title : super.getTitle();
	}
	
	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent(html);
	}
}
