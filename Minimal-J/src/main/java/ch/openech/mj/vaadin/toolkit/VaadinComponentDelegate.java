package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.IComponentDelegate;

import com.vaadin.ui.Component;

public class VaadinComponentDelegate implements IComponentDelegate {

	private final Component component;
	
	public VaadinComponentDelegate(Component component) {
		this.component = component;
	}

	@Override
	public Component getComponent() {
		return component;
	}
	
}
