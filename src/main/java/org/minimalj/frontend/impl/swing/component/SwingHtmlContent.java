package org.minimalj.frontend.impl.swing.component;

import java.io.IOException;
import java.net.URL;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.StringUtils;

public class SwingHtmlContent extends JTextPane implements IContent {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_TYPE_CLICK = "click";

	public SwingHtmlContent(String htmlOrUrl) {
		setContentType("text/html");
		setEditable(false);

		String url, html;
		if (StringUtils.isHtml(htmlOrUrl)) {
			url = null;
			html = htmlOrUrl;
		} else if (StringUtils.isUrl(htmlOrUrl)) {
			url = htmlOrUrl;
			html = null;
		} else if (htmlOrUrl.endsWith(".html")) {
			URL resource = getClass().getClassLoader().getResource(htmlOrUrl);
			if (resource != null) {
				url = resource.toExternalForm();
				html = null;
			} else {
				throw new IllegalArgumentException("Invalid content: " + htmlOrUrl);
			}
		} else {
			url = null;
			html = "<html>" + StringUtils.escapeHTML(htmlOrUrl) + "</html>";
		}

		if (html != null) {
			setText(html);
		} else {
			try {
				setPage(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		HyperlinkListener listener = new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == EventType.ACTIVATED) {
					String href = e.getDescription();
					if (href.startsWith("/")) {
						href = href.substring(1);
					}
					Page page = Application.getInstance().createPage(href);
					if (page != null) {
						SwingFrontend.runWithContext(() -> Frontend.show(page));
					}
				}
			}
		};
		addHyperlinkListener(listener);
	}

}