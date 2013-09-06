package ch.openech.mj.lanterna.toolkit;

import ch.openech.mj.toolkit.IComponent;

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
