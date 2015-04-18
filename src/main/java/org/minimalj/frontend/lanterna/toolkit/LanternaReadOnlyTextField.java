package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.toolkit.IFocusListener;
import org.minimalj.frontend.toolkit.TextField;

import com.googlecode.lanterna.gui.component.Label;

public class LanternaReadOnlyTextField extends Label implements TextField {

	public LanternaReadOnlyTextField() {
		super();
		setAlignment(Alignment.LEFT_CENTER);
	}

	@Override
	public void setEditable(boolean editable) {
		// ignored
	}

	@Override
	public void setFocusListener(IFocusListener focusListener) {
		// ignored
	}

	@Override
	public void setCommitListener(Runnable listener) {
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
