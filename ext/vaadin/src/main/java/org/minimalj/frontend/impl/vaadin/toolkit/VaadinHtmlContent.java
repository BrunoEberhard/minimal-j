package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;

import com.vaadin.server.ClassResource;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

public class VaadinHtmlContent extends CustomComponent implements IContent {
	private static final long serialVersionUID = 1L;

	public VaadinHtmlContent(String htmlOrUrl) {
		if (htmlOrUrl.startsWith("<")) {
			Label label = new Label(htmlOrUrl, ContentMode.HTML);
			label.setSizeFull();
			setCompositionRoot(label);
		} else if (StringUtils.isUrl(htmlOrUrl)) {
			BrowserFrame embedded = new BrowserFrame(null, new ExternalResource(htmlOrUrl));
			embedded.setSizeFull();
			setCompositionRoot(embedded);
		} else if (htmlOrUrl.endsWith(".html")) {
			BrowserFrame embedded = new BrowserFrame(null, new ClassResource("/" + htmlOrUrl));
			embedded.setSizeFull();
			setCompositionRoot(embedded);
		} else {
			Label label = new Label(htmlOrUrl, ContentMode.TEXT);
			label.setSizeFull();
			setCompositionRoot(label);
			setCompositionRoot(new Label(htmlOrUrl));
		}
	}

}
