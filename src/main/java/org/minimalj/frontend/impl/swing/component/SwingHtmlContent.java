package org.minimalj.frontend.impl.swing.component;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class SwingHtmlContent extends JFXPanel implements IContent {
	private static final long serialVersionUID = 1L;

	public SwingHtmlContent(String htmlOrUrl) {
		Platform.setImplicitExit(false);
		
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
				if (htmlOrUrl.startsWith("<")) {
					webEngine.loadContent(htmlOrUrl);
				} else if (StringUtils.isUrl(htmlOrUrl)) {
					webEngine.load(htmlOrUrl);
				} else {
					webEngine.load(getClass().getClassLoader().getResource(htmlOrUrl).toExternalForm());
				}
				setScene(scene);
			}
		});
	}
	

}
