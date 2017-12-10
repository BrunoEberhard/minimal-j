package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.Input;

import com.googlecode.lanterna.gui2.Label;

public class LanternaReadOnlyTextField extends Label implements Input<String> {

	public LanternaReadOnlyTextField() {
		super("");
	}

	@Override
	public void setEditable(boolean editable) {
		// ignored
	}
	
	@Override
	public void setValue(String text) {
		if (text == null) {
			text = "";
		}
		super.setText(text);
	}

	@Override
	public String getValue() {
		return getText();
	}

}
