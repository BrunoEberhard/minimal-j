package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IComponent;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinHorizontalLayout extends GridLayout implements HorizontalLayout {

	public VaadinHorizontalLayout(IComponent[] components) {
		super(components.length, 1);
		
		for (IComponent c : components) {
			Component component = (Component) c;
			component.setWidth("100%");
			addComponent(component);
		}
	}

}
