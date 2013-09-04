package ch.openech.mj.lanterna.toolkit;

import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IComponent;

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
