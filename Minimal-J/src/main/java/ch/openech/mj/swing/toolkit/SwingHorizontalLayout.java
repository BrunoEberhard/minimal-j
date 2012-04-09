package ch.openech.mj.swing.toolkit;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;

import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IComponent;


public class SwingHorizontalLayout extends JPanel implements HorizontalLayout {

	public SwingHorizontalLayout(IComponent... components) {
		super(new GridLayout(1, components.length, 10, 10));
		for (IComponent c : components) {
			Component component = SwingClientToolkit.getComponent(c);
			add(component);
		}
		setInheritsPopupMenu(true);
	}
	
}
