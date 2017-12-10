package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.util.StringUtils;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;

public class LanternaTextField extends TextBox implements Input<String> {

	private final InputComponentListener changeListener;
	
	private String textOnFocusLost;

	public LanternaTextField(InputComponentListener changeListener, Style style) {
		super(new TerminalSize(10, style == Style.MULTI_LINE ? 3 : 1),  "", style);
		this.changeListener = changeListener;
	}

	@Override
	public void setEditable(boolean editable) {
		super.setReadOnly(!editable);
	}

	@Override
	public String getValue() {
		String text = super.getText();
		if (text.length() == 0) return null;
		return text;
	}

	@Override
	public void setValue(String text) {
		if (text == null) {
			text = "";
		}
		textOnFocusLost = text;
		if (!isFocused()) {
			super.setText(text);
		}
	}
	
	private void fireChangeEvent() {
		textOnFocusLost = super.getText();
		changeListener.changed(LanternaTextField.this);
	}
	
	@Override
	public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
		Result result = super.handleKeyStroke(keyStroke);
		if (result != Result.UNHANDLED) {
			fireChangeEvent();
		}
		return result;
	}
	
	@Override
	protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
		if (!StringUtils.equals(textOnFocusLost, getText())) {
			setText(textOnFocusLost);
		}
	}
	
	@Override
	protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
		textOnFocusLost = getText();
	}
	
}
