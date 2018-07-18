package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.Frontend.SwitchContent;

public class SwingSwitch extends JPanel implements SwitchContent, SwitchComponent {
	private static final long serialVersionUID = 1L;
	
	private JComponent shownComponent;
	
	public SwingSwitch() {
		super(new BorderLayout());
		setInheritsPopupMenu(true);
	}

	@Override
	public void show(IContent content) {
		show((JComponent) content);
	}

	@Override
	public void show(IComponent component) {
		show((JComponent) component);
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
