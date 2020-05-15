package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinVerticalLayout extends GridLayout implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinVerticalLayout(IComponent[] components) {
		super(1, components.length);
		
		for (IComponent c : components) {
			Component component = (Component) c;
			component.setWidth("100%");
			addComponent(component);
		}
	}

}
