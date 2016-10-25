package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IContent;

import com.vaadin.server.ClassResource;
import com.vaadin.ui.BrowserFrame;

public class VaadinUrlContent extends BrowserFrame implements IContent {

	public VaadinUrlContent(String htmlOrUrl) {
		super(null, new ClassResource(htmlOrUrl));
	}

}
