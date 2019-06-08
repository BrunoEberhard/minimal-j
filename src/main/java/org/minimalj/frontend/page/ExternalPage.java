package org.minimalj.frontend.page;

import java.net.MalformedURLException;
import java.net.URL;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;

/**
 * Note: If you use this Page as a detail the web frontend may not be able to
 * calculate the height for security reason. This may produce a double
 * scrollbar.
 */
public class ExternalPage extends Page {

	private final URL url;
	private final String title;

	public ExternalPage(String urlString) {
		this(urlString, null);
	}
	
	public ExternalPage(String urlString, String title) {
		this.title = title;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public ExternalPage(URL url, String title) {
		this.title = title;
		this.url = url;
	}

	@Override
	public String getTitle() {
		return title != null ? title : super.getTitle();
	}
	
	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent(url);
	}
}
