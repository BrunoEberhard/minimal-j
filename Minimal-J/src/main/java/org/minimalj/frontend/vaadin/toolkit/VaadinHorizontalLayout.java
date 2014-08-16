package org.minimalj.frontend.vaadin.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinHorizontalLayout extends GridLayout implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinHorizontalLayout(IComponent[] components) {
		super(components.length, 1);
		
		for (IComponent c : components) {
			Component component = (Component) c;
			component.setWidth("100%");
			addComponent(component);
		}
	}

}
