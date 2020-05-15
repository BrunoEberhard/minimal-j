package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class VaadinVerticalLayout extends VerticalLayout implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinVerticalLayout(IComponent[] components) {
		for (IComponent c : components) {
			Component component = (Component) c;
			((HasSize) component).setWidthFull();
			add(component);
		}
	}

}
