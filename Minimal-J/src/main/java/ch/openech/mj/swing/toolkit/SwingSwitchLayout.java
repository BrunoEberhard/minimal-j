package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;

public class SwingSwitchLayout extends JPanel implements SwitchLayout {

	private IComponent shownComponent;
	
	public SwingSwitchLayout() {
		super(new BorderLayout());
		setInheritsPopupMenu(true);
	}

	@Override
	public void show(IComponent c) {
		if (shownComponent != c) {
			Component component = SwingClientToolkit.getComponent(c);
			removeAll();
			add(component, BorderLayout.CENTER);
			revalidate();
			repaint();
		}
		shownComponent = c;
	}

	@Override
	public IComponent getShownComponent() {
		return shownComponent;
	}

	@Override
	public void requestFocus() {
		if (shownComponent != null) {
			Component component = SwingClientToolkit.getComponent(shownComponent);
			component.requestFocus();
		}
	}
	
}
