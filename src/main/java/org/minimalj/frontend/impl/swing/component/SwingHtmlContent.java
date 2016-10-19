package org.minimalj.frontend.impl.swing.component;

import java.net.URL;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

@SuppressWarnings("restriction")
public class SwingHtmlContent extends JFXPanel implements IContent {
	private static final long serialVersionUID = 1L;

	public SwingHtmlContent(String htmlOrUrl) {
		Platform.setImplicitExit(false);

		String url, html;
		if (htmlOrUrl.startsWith("<")) {
			url = null;
			html = htmlOrUrl;
		} else if (StringUtils.isUrl(htmlOrUrl)) {
			url = htmlOrUrl;
			html = null;
		} else {
			URL resource = getClass().getClassLoader().getResource(htmlOrUrl);
			if (resource != null) {
				url = resource.toExternalForm();
				html = null;
			} else {
				throw new IllegalArgumentException("Invalid content: " + htmlOrUrl);
			}
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
			}
		});
	}
	
}