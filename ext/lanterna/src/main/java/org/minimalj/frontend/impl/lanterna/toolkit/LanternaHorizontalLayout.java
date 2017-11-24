package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.IComponent;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

public class LanternaHorizontalLayout extends Panel implements IComponent {

	public LanternaHorizontalLayout(IComponent[] components) {
		setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
		for (IComponent component : components) {
			Component c = (Component) component;
			addComponent(c);
		}
	}

}
