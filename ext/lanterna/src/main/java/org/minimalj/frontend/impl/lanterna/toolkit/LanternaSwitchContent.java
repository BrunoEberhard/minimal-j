package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchContent;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;

public class LanternaSwitchContent extends Panel implements SwitchContent {

	@Override
	public void show(IContent content) {
		show((Component) content);
	}
	
	public void show(Component component) {
		super.removeAllComponents();
		if (component != null) {
			super.addComponent(component);
		}
		invalidate();
	}


}
