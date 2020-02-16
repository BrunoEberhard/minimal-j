package org.minimalj.frontend.impl.swing.component;

import java.net.URL;

import javax.swing.SwingUtilities;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Routing;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLImageElement;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class FxHtmlContent extends javafx.embed.swing.JFXPanel implements IContent {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_TYPE_CLICK = "click";

	public FxHtmlContent(String html) {
		Platform.setImplicitExit(false);
		startBrowser(null, html);
	}

	public FxHtmlContent(URL url) {
		Platform.setImplicitExit(false);
		startBrowser(url.toExternalForm(), null);
	}

	private void startBrowser(String url, String html) {
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
									String href = findHref(ev);
									if (href == null) {
										return;
									}

									Page page = Routing.createPageSafe(href);
									SwingUtilities.invokeLater(() -> {
										SwingFrontend.run(FxHtmlContent.this, () -> Frontend.show(page));
									});
								}
							}

						};

						Document doc = webBrowser.getEngine().getDocument();
						NodeList nodeList = doc.getElementsByTagName("a");
						for (int i = 0; i < nodeList.getLength(); i++) {
							((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, listener, true);
						}

						if (url == null) {
							nodeList = doc.getElementsByTagName("img");
							for (int i = 0; i < nodeList.getLength(); i++) {
								HTMLImageElement n = (HTMLImageElement) nodeList.item(i);
								String src = n.getSrc();
								if (src.startsWith("/")) {
									src = src.substring(1);
								}
								n.setSrc(getClass().getClassLoader().getResource(src).toExternalForm());
							}
						}
					}
				});
			}
		});
	}

	/*
	 * If the 'A' tag contains inner tags the click doesn't report the 'A' Element
	 * but the inner tag as target. This method tries to find the 'hyper reference'
	 */
	private static String findHref(Event ev) {
		Element element = (Element) ev.getTarget();
		while (element != null && !element.hasAttribute("href")) {
			element = (Element) element.getParentNode();
		}
		if (element == null) {
			return null;
		}
		String href = element.getAttribute("href");
		return href;
	}

}