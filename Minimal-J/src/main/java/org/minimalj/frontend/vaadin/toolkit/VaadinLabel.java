package org.minimalj.frontend.vaadin.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

import com.vaadin.ui.Label;

public class VaadinLabel extends Label implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinLabel(String content) {
		super(content);
		setContentMode(Label.CONTENT_XHTML);
	}

}
