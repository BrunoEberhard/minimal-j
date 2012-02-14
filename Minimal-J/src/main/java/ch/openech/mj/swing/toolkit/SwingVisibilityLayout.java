package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.VisibilityLayout;

public class SwingVisibilityLayout extends JPanel implements VisibilityLayout {

	public SwingVisibilityLayout(IComponent content) {
		super(new BorderLayout());
		Component component = SwingClientToolkit.getComponent(content);
		add(component, BorderLayout.CENTER);
	}

	@Override
	public void requestFocus() {
		for (Component c : getComponents()) {
			c.requestFocus();
			break;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

	@Override
	public boolean isVisible() {
		return super.isVisible();
	}
	
}
