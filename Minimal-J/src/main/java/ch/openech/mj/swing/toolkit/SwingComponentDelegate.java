package ch.openech.mj.swing.toolkit;

import java.awt.Component;

import ch.openech.mj.toolkit.IComponentDelegate;

public class SwingComponentDelegate implements IComponentDelegate {

	private final Component component;
	
	public SwingComponentDelegate(Component component) {
		this.component = component;
	}

	@Override
	public Component getComponent() {
		return component;
	}
	
}
