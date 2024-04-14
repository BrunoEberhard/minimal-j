package org.minimalj.frontend.impl.vaadin.toolkit;

import java.net.URL;

import org.minimalj.frontend.Frontend.IContent;

import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

public class VaadinHtmlContent extends CustomComponent implements IContent {
	private static final long serialVersionUID = 1L;

	public VaadinHtmlContent(String htmlOrUrl) {
		Label label = new Label(htmlOrUrl, ContentMode.HTML);
		label.setSizeFull();
		setCompositionRoot(label);
	}

	public VaadinHtmlContent(URL url) {
		// TODO can this make local images work?
		// BrowserFrame embedded = new BrowserFrame(null, new ClassResource("/" + htmlOrUrl));

		BrowserFrame embedded = new BrowserFrame(null, new ExternalResource(url));
		embedded.setSizeFull();
		setCompositionRoot(embedded);
	}

}
