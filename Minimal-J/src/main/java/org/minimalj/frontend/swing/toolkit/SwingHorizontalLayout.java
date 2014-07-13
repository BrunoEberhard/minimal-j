package org.minimalj.frontend.swing.toolkit;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.minimalj.frontend.toolkit.HorizontalLayout;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;


public class SwingHorizontalLayout extends JPanel implements HorizontalLayout {
	private static final long serialVersionUID = 1L;

	public SwingHorizontalLayout(IComponent... components) {
		super(new GridLayout(1, components.length, 10, 10));
		for (IComponent c : components) {
			Component component = (Component) c;
			add(component);
		}
		setInheritsPopupMenu(true);
	}
	
}
