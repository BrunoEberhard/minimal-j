package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import ch.openech.mj.toolkit.VisibilityLayout;

public class SwingVisibilityLayout extends JPanel implements VisibilityLayout {

	public SwingVisibilityLayout(Object content) {
		super(new BorderLayout());
		add((Component) content, BorderLayout.CENTER);
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
