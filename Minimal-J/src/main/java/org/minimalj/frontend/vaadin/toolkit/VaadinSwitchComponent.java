package org.minimalj.frontend.vaadin.toolkit;

import java.util.Arrays;
import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.SwitchComponent;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinSwitchComponent extends GridLayout implements SwitchComponent {
	private static final long serialVersionUID = 1L;

	private final List<IComponent> components;
	private IComponent showComponent;
	
	public VaadinSwitchComponent(IComponent... components) {
		this.components = Arrays.asList(components);
	}
	
	public void requestFocus() {
		if (showComponent instanceof Focusable) {
			((Focusable) showComponent).focus();
		}
	}

	@Override
	public void show(IComponent c) {
		if (!components.contains(c)) throw new IllegalArgumentException("Component not specified at constructor");
		if (showComponent != null) {
			Component component = (Component) showComponent;
			removeComponent(component);
		}

		if (c != null) {
			Component component = (Component) c;
			component.setWidth("100%");
			addComponent(component);
			VaadinClientToolkit.focusFirstComponent(component);
		}
		this.showComponent = c;
	}

	@Override
	public IComponent getShownComponent() {
		return showComponent;
	}

}
