package ch.openech.mj.vaadin.toolkit;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

import ch.openech.mj.toolkit.HorizontalLayout;

public class VaadinHorizontalLayout extends GridLayout implements HorizontalLayout {

	public VaadinHorizontalLayout(Object[] components) {
		super(components.length, 1);
		
		for (Object c : components) {
			Component component = (Component) c;
			component.setWidth("100%");
			addComponent(component);
		}
	}

}
