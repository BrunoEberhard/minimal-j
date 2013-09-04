package ch.openech.mj.lanterna.toolkit;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.TextGraphics;
import com.googlecode.lanterna.gui.component.AbstractContainer;
import com.googlecode.lanterna.terminal.TerminalSize;

public class LanternaSwitchLayout extends AbstractContainer implements SwitchLayout {

	@Override
	public void show(IComponent component) {
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
