package org.minimalj.frontend.impl.swing.component;

import java.net.URL;

import javax.swing.SwingUtilities;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class FxHtmlContent extends javafx.embed.swing.JFXPanel implements IContent {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_TYPE_CLICK = "click";

	public FxHtmlContent(String htmlOrUrl) {
		Platform.setImplicitExit(false);

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

		PlatformImpl.startup(new Runnable() {
			@Override
			public void run() {
				WebView webBrowser = new WebView();
				AnchorPane.setTopAnchor(webBrowser, 0.0);
				AnchorPane.setBottomAnchor(webBrowser, 0.0);
				AnchorPane.setLeftAnchor(webBrowser, 0.0);
				AnchorPane.setRightAnchor(webBrowser, 0.0);

				AnchorPane anchorPane = new AnchorPane();
				anchorPane.getChildren().add(webBrowser);

				Scene scene = new Scene(anchorPane);
				WebEngine webEngine = webBrowser.getEngine();
				if (html != null) {
					webEngine.loadContent(html);
				} else {
					webEngine.load(url);
				}
				setScene(scene);

				webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
					if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
						EventListener listener = new EventListener() {
							@Override
							public void handleEvent(Event ev) {
								String domEventType = ev.getType();
								if (domEventType.equals(EVENT_TYPE_CLICK)) {
									String href = ((Element) ev.getTarget()).getAttribute("href");
									if (href.startsWith("/")) {
										href = href.substring(1);
									}
									Page page = Application.getInstance().createPage(href);
									if (page != null) {
										SwingUtilities.invokeLater(() -> {
											SwingFrontend.runWithContext(() -> Frontend.show(page));
										});
									}
								}
							}
						};

						Document doc = webBrowser.getEngine().getDocument();
						NodeList nodeList = doc.getElementsByTagName("a");
						for (int i = 0; i < nodeList.getLength(); i++) {
							((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, listener, true);
						}
					}
				});

			}
		});
	}

}