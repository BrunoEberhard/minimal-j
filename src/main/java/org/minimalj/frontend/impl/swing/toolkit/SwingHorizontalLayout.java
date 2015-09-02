package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.minimalj.frontend.Frontend.IComponent;


public class SwingHorizontalLayout extends JPanel implements IComponent {
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
