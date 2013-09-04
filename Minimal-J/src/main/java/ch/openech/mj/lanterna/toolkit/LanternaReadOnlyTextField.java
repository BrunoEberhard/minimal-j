package ch.openech.mj.lanterna.toolkit;

import java.awt.event.FocusListener;

import ch.openech.mj.toolkit.TextField;

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
	public void setFocusListener(FocusListener focusListener) {
		// ignored
	}

	@Override
	public void setCommitListener(Runnable listener) {
		// ignored
	}
	
	@Override
	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		super.setText(text);
	}

}
