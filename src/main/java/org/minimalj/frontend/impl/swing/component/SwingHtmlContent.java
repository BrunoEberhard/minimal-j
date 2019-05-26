package org.minimalj.frontend.impl.swing.component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;
import javax.swing.text.html.ImageView;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Routing;
import org.minimalj.util.StringUtils;

public class SwingHtmlContent extends JTextPane implements IContent {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_TYPE_CLICK = "click";

	public SwingHtmlContent(String htmlOrUrl) {
		setEditorKit(new MjHtmlEditorKit());
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

		HyperlinkListener listener = e -> {
			if (e.getEventType() == EventType.ACTIVATED) {
				String href = e.getDescription();
				if (href.startsWith("/")) {
					href = href.substring(1);
				}
				Page page = Routing.createPageSafe(href);
				if (page != null) {
					SwingFrontend.runWithContext(() -> Frontend.show(page));
				}
			}
		};
		addHyperlinkListener(listener);
	}

	private static class MjHtmlEditorKit extends HTMLEditorKit {
		private static final long serialVersionUID = 1L;

		private static ViewFactory mjFactory = new MjHTMLFactory();

		public ViewFactory getViewFactory() {
			return mjFactory;
		}
	}

	private static class MjHTMLFactory extends HTMLFactory {
		@Override
		public View create(Element elem) {
			Object tag = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
			if (tag instanceof HTML.Tag) {
				HTML.Tag kind = (HTML.Tag) tag;
				if (kind == HTML.Tag.IMG) {
					return new MjImageView(elem);
				}
			}
			return super.create(elem);
		}
	}

	private static class MjImageView extends ImageView {

		public MjImageView(Element elem) {
			super(elem);
		}

		public URL getImageURL() {
			String src = (String) getElement().getAttributes().getAttribute(HTML.Attribute.SRC);
			if (src == null) {
				return null;
			}

			URL reference = ((HTMLDocument) getDocument()).getBase();
			try {
				if (reference != null) {
					return new URL(reference, src);
				} else {
					// this is the line
					return getClass().getClassLoader().getResource(src);
				}
			} catch (MalformedURLException e) {
				return null;
			}
		}
	}

}