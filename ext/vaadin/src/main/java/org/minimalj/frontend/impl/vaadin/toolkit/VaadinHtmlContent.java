package org.minimalj.frontend.impl.vaadin.toolkit;

import java.net.URL;

import org.minimalj.frontend.Frontend.IContent;

import com.vaadin.flow.component.html.IFrame;

public class VaadinHtmlContent extends IFrame implements IContent {
	private static final long serialVersionUID = 1L;

	private VaadinHtmlContent() {
		setSizeFull();
		getElement().getStyle().set("border-width", "0");
	}
	
	public VaadinHtmlContent(String html) {
		this();
		setSrcdoc(html);
	}

	public VaadinHtmlContent(URL url) {
		this();
		setSrc(url.toString());
	}

}
