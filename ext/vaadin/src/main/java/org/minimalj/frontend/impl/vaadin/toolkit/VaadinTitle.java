package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;

import com.vaadin.flow.component.html.H2;

public class VaadinTitle extends H2 implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinTitle(String content) {
		setText(content);
	}

}
