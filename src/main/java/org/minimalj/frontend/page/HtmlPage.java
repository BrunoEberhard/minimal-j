package org.minimalj.frontend.page;

import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * <b>note:</b> for security reasons read the JavaDoc in the Frontend class
 * 
 * @see Frontend#createHtmlContent(String)
 * @see StringUtils#escapeHTML(String)
 */
public class HtmlPage implements Page {
	private static final Logger logger = Logger.getLogger(HtmlPage.class.getName());

	private final String html;
	private final String route;
	private transient String title;

	public HtmlPage(String html) {
		this(html, null);
	}

	public HtmlPage(String html, String route) {
		this.html = html;
		this.route = route;
	}

	public HtmlPage title(String title) {
		this.title = title;
		return this;
	}

	@Override
	public String getTitle() {
		if (title == null) {
			try {
				Reader stringReader = new StringReader(html);
				HTMLEditorKit htmlKit = new HTMLEditorKit();
				HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
				HTMLEditorKit.Parser parser = new ParserDelegator();
				parser.parse(stringReader, htmlDoc.getReader(0), true);

				Object propertyTitle = htmlDoc.getProperty("title");
				if (propertyTitle != null) {
					title = propertyTitle.toString();
				}
			} catch (Exception x) {
				if (Configuration.isDevModeActive()) {
					System.out.println(html);
					logger.log(Level.WARNING, "Title of page could not be extracted", x);
				} else {
					logger.log(Level.FINE, "Title of page could not be extracted", x);
				}
			}
		}
		if (title == null) {
			title = Resources.getPageTitle(this);
		}
		return title;
	}

	protected String getHtml() {
		// can be overridden if content is dynamic
		return html;
	}

	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent(getHtml());
	}

	public String getRoute() {
		return route;
	}
}
