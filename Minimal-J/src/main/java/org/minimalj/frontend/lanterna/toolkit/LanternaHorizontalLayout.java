package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.toolkit.HorizontalLayout;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.layout.HorisontalLayout;

public class LanternaHorizontalLayout extends Panel implements HorizontalLayout {

	public LanternaHorizontalLayout(IComponent[] components) {
		setLayoutManager(new HorisontalLayout());
		for (IComponent component : components) {
			Component c = (Component) component;
			addComponent(c);
		}
	}

}
