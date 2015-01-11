package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.SwitchContent;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.TextGraphics;
import com.googlecode.lanterna.gui.component.AbstractContainer;
import com.googlecode.lanterna.terminal.TerminalSize;

public class LanternaSwitchContent extends AbstractContainer implements SwitchContent {

	@Override
	public void show(IContent content) {
		show((Component) content);
	}
	
	public void show(Component component) {
		super.removeAllComponents();
		if (component != null) {
			super.addComponent(component);
		}
		invalidate();
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
