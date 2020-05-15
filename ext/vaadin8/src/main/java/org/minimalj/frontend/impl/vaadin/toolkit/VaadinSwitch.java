package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.Frontend.SwitchContent;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinSwitch extends GridLayout implements SwitchContent, SwitchComponent {
	private static final long serialVersionUID = 1L;

	private Component current;
	
	public VaadinSwitch() {
	}
	
	public void requestFocus() {
		if (current instanceof Focusable) {
			((Focusable) current).focus();
		}
	}

	@Override
	public void show(IContent content) {
		show((Component) content);
	}
	
	@Override
	public void show(IComponent component) {
		show((Component) component);
	}

	private void show(Component component) {
		if (current != null) {
			removeComponent(current);
		}

		if (component != null) {
			component.setWidth("100%");
			addComponent(component);
			VaadinFrontend.focusFirstComponent(component);
		}
		this.current = component;
	}

}
