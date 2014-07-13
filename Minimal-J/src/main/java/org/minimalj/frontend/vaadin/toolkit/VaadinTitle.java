package org.minimalj.frontend.vaadin.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

import com.vaadin.ui.Label;

public class VaadinTitle extends Label implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinTitle(String content) {
//		content = Util.escapeHTML(content);
		setContentMode(CONTENT_XHTML);
		setValue("<h1>" + content + "</h1><hr />");
	}

}
