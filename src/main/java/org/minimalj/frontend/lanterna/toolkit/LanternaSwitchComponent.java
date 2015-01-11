package org.minimalj.frontend.lanterna.toolkit;

import java.util.Arrays;
import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.SwitchComponent;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.TextGraphics;
import com.googlecode.lanterna.gui.component.AbstractContainer;
import com.googlecode.lanterna.terminal.TerminalSize;

public class LanternaSwitchComponent extends AbstractContainer implements SwitchComponent {

	private final List<IComponent> components;
	
	public LanternaSwitchComponent(IComponent... components) {
		this.components = Arrays.asList(components);
	}

	@Override
	public void show(IComponent component) {
		if (!components.contains(component)) throw new IllegalArgumentException("Component not specified at constructor");
		super.removeAllComponents();
		if (component != null) {
			super.addComponent((Component) component);
		}
		invalidate();
	}

	@Override
	public IComponent getShownComponent() {
		if (getComponentCount() > 0) {
			return (IComponent) getComponentAt(0);
		} else {
			return null;
		}
	}

	@Override
	protected TerminalSize calculatePreferredSize() {
		if (getComponentCount() > 0) {
			return getComponentAt(0).getPreferredSize();
		} else {
			return new TerminalSize(0, 0);
		}
	}

	@Override
	public void repaint(TextGraphics graphics) {
		if (getComponentCount() > 0) {
			getComponentAt(0).repaint(graphics);
		} 
	}

}
