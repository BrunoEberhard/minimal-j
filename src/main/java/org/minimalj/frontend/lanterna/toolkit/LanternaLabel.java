package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

import com.googlecode.lanterna.gui.component.Label;

public class LanternaLabel extends Label implements IComponent {

	public LanternaLabel(String text) {
		super(text);
	}

	@Override
	public void setText(String text) {
		super.setText(text != null ? text : "");
	}
	
}
