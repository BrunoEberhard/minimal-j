package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import ch.openech.mj.toolkit.SwitchLayout;

public class SwingSwitchLayout extends JPanel implements SwitchLayout {

	private JComponent shownComponent;
	
	public SwingSwitchLayout() {
		super(new BorderLayout());
	}

	@Override
	public void show(Object component) {
		if (shownComponent != component) {
			removeAll();
			add((Component) component, BorderLayout.CENTER);
			revalidate();
			repaint();
		}
		shownComponent = (JComponent) component;
	}

	@Override
	public Object getShownComponent() {
		return shownComponent;
	}

	@Override
	public void requestFocus() {
		if (shownComponent != null) {
			shownComponent.requestFocus();
		}
	}
	
}
