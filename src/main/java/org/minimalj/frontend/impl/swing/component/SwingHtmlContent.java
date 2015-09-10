package org.minimalj.frontend.impl.swing.component;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;

import com.sun.javafx.application.PlatformImpl;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class SwingHtmlContent extends JFXPanel implements IContent {
	private static final long serialVersionUID = 1L;

	public SwingHtmlContent(String htmlOrUrl) {
		PlatformImpl.startup(new Runnable() {
			@Override
			public void run() {
				final AnchorPane anchorPane = new AnchorPane();
				WebView webBrowser = new WebView();

				// Set Layout Constraint
				AnchorPane.setTopAnchor(webBrowser, 0.0);
				AnchorPane.setBottomAnchor(webBrowser, 0.0);
				AnchorPane.setLeftAnchor(webBrowser, 0.0);
				AnchorPane.setRightAnchor(webBrowser, 0.0);

				// Add WebView to AnchorPane
				anchorPane.getChildren().add(webBrowser);

				// Create Scene
				final Scene scene = new Scene(anchorPane);

				// Obtain the webEngine to navigate
				final WebEngine webEngine = webBrowser.getEngine();
				if (StringUtils.isUrl(htmlOrUrl)) {
					webEngine.load(htmlOrUrl);
				} else {
					webEngine.loadContent(htmlOrUrl);
				}
				setScene(scene);
			}
		});
	}
	

}
