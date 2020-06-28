package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class VaadinHorizontalLayout extends HorizontalLayout implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinHorizontalLayout(IComponent[] components) {
		VaadinVerticalLayout.addAll(this, components);
	}
	
}
