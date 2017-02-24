package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;

import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;

public class VaadinTitle extends Label implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinTitle(String content) {
//		content = Util.escapeHTML(content);
		setContentMode(ContentMode.HTML);
		setValue("<h2>" + content + "</h2><hr />");
	}

}
