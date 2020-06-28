package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasComponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class VaadinVerticalLayout extends VerticalLayout implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinVerticalLayout(IComponent[] components) {
		addAll(this, components);
	}

	static void addAll(HasComponents hasComponents, IComponent[] components) {
		for (IComponent c : components) {
			Component component = c instanceof HasComponent ? ((HasComponent) c).getComponent() : (Component) c;
			if (component instanceof HasSize) {
				((HasSize) component).setWidthFull();
			}
			hasComponents.add(component);
		}
	}

}
