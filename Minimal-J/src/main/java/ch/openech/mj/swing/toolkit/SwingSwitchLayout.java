package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;

public class SwingSwitchLayout extends JPanel implements SwitchLayout {
	private static final long serialVersionUID = 1L;
	
	private IComponent shownComponent;
	
	public SwingSwitchLayout() {
		super(new BorderLayout());
		setInheritsPopupMenu(true);
	}

	@Override
	public void show(IComponent c) {
		if (shownComponent != c) {
			Component component = (Component) c;
			removeAll();
			if (component != null) {
				// TODO the updateComponentTreeUI uses 4-5ms. This should only be done if necessary (if l&f has changed)
				SwingUtilities.updateComponentTreeUI(component);
				add(component, BorderLayout.CENTER);
				refresh(component);
			} else {
				refresh(this);
			}
		}
		shownComponent = c;
	}

	private void refresh(Component component) {
		SwingInternalFrame swingInternalFrame = getInternalFrameAncestor(component);
		if (swingInternalFrame != null) {
			swingInternalFrame.pack();
		} else {
			Window window = SwingUtilities.getWindowAncestor(component);
			if (window instanceof Dialog) {
				window.pack();
			} else {
				revalidate();
				repaint();
			}
		}
	}

	private SwingInternalFrame getInternalFrameAncestor(Component component) {
		for (Container p = component.getParent(); p != null; p = p.getParent()) {
			if (p instanceof SwingInternalFrame) {
				return (SwingInternalFrame) p;
			}
		}
		return null;
	}

	@Override
	public IComponent getShownComponent() {
		return shownComponent;
	}

	@Override
	public void requestFocus() {
		if (shownComponent != null) {
			Component component = (Component) shownComponent;
			component.requestFocus();
		}
	}
	
}
