package org.minimalj.frontend.impl.swing.component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Routing;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLElement;
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
								try {
									n.setSrc(WebApplication.getResourceHandler().getUrl(src).toExternalForm());
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
							nodeList = doc.getElementsByTagName("embed");
							for (int i = 0; i < nodeList.getLength(); i++) {
								HTMLElement n = (HTMLElement) nodeList.item(i);
								String src = n.getAttribute("src");
								WebApplicationPageExchange exchange = new WebApplicationPageExchange(src);
								WebApplication.getWebApplicationHandler().handle(exchange);
								StringBuilder s = new StringBuilder();
								s.append("data:").append(exchange.getContentType()).append(";base64,").append(exchange.getResult());
								System.out.println(s.toString());
								n.setAttribute("src", s.toString());
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

	public static class WebApplicationPageExchange extends MjHttpExchange {
		private final String path;
		private byte[] result;
		private String contentType;

		public WebApplicationPageExchange(String path) {
			this.path = path;
		}

		public String getResult() {
			return Base64.getEncoder().encodeToString(result);
		}

		public String getContentType() {
			return contentType;
		}
		
		@Override
		public String getPath() {
			return path;
		}

		@Override
		public InputStream getRequest() {
			return null;
		}

		@Override
		public Map<String, List<String>> getParameters() {
			return Collections.emptyMap();
		}

		@Override
		public void sendResponse(int statusCode, byte[] bytes, String contentType) {
			this.result = bytes;
			this.contentType = contentType;
		}

		@Override
		public void sendResponse(int statusCode, String response, String contentType) {
			this.result = response.getBytes(Charset.forName("utf-8"));
			this.contentType = contentType;
		}

		@Override
		public boolean isResponseSent() {
			return result != null;
		}
	}
}