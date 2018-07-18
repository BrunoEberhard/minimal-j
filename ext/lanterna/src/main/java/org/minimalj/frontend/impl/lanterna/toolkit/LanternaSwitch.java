package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.Frontend.SwitchContent;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;

public class LanternaSwitch extends Panel implements SwitchContent, SwitchComponent {

	@Override
	public void show(IContent content) {
		show((Component) content);
	}
	
	@Override
	public void show(IComponent component) {
		show((Component) component);
	}
	
	public void show(Component component) {
		super.removeAllComponents();
		if (component != null) {
			super.addComponent(component);
		}
		invalidate();
	}

}
