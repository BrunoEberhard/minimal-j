package ch.openech.mj.vaadin.toolkit;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IComponent;

public class VaadinHorizontalLayout extends GridLayout implements HorizontalLayout {

	public VaadinHorizontalLayout(IComponent[] components) {
		super(components.length, 1);
		
		for (IComponent c : components) {
			Component component = VaadinClientToolkit.getComponent(c);
			component.setWidth("100%");
			addComponent(component);
		}
	}

}
