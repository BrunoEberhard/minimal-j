package org.minimalj.frontend.impl.vaadin.toolkit;

import java.net.URL;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Embedded;

public class VaadinHtmlContent extends Embedded implements IContent {

	public VaadinHtmlContent(String htmlOrUrl) {
		String url, html;
		if (htmlOrUrl.startsWith("<")) {
			url = null;
			html = htmlOrUrl;
		} else if (StringUtils.isUrl(htmlOrUrl)) {
			setSource(new ExternalResource(htmlOrUrl));
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
	}

}
