package org.minimalj.frontend.page;

import java.net.MalformedURLException;
import java.net.URL;

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
	private final URL url;
	private final String title;
	
	public HtmlPage(String html, String title) {
		this.html = html;
		this.title = title;
		this.url = null;
	}

	public HtmlPage(URL url, String title) {
		this.html = null;
		this.title = title;
		this.url = url;
	}

	public static HtmlPage fromUrl(String urlString, String title) {
		try {
			URL url = new URL(urlString);
			return new HtmlPage(url, title);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public IContent getContent() {
		if (html != null) {
			return Frontend.getInstance().createHtmlContent(html);
		} else {
			return Frontend.getInstance().createHtmlContent(url);
		}
	}
}
