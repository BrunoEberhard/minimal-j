package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchContent;

public class SwingSwitchContent extends JPanel implements SwitchContent {
	private static final long serialVersionUID = 1L;
	
	private JComponent shownComponent;
	
	public SwingSwitchContent() {
		super(new BorderLayout());
		setInheritsPopupMenu(true);
	}

	@Override
	public void show(IContent c) {
		show((JComponent) c);
	}
	
	public void show(JComponent component) {
		if (shownComponent != component) {
			removeAll();
			if (component != null) {
				add(component, BorderLayout.CENTER);
			}
			refresh(this);
		}
		shownComponent = component;
	}
	
	private void refresh(Component component) {
		repaint();
		revalidate();
	}

	@Override
	public void requestFocus() {
		if (shownComponent != null) {
			shownComponent.requestFocus();
		}
	}

}
