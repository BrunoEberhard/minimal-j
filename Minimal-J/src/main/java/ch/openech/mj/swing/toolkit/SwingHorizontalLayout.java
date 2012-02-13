package ch.openech.mj.swing.toolkit;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;

import ch.openech.mj.toolkit.HorizontalLayout;


public class SwingHorizontalLayout extends JPanel implements HorizontalLayout {

	public SwingHorizontalLayout(Object... components) {
		super(new GridLayout(1, components.length));
		for (Object c : components) {
			Component component = (Component) c;
			add(component);
		}
	}
	
}
