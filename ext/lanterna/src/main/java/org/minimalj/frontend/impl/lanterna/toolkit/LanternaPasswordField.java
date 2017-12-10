package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.PasswordField;
import org.minimalj.util.StringUtils;

import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;

public class LanternaPasswordField extends TextBox implements PasswordField {

	private final InputComponentListener changeListener;
	
	private String textOnFocusLost;

	public LanternaPasswordField(InputComponentListener changeListener) {
		this.changeListener = changeListener;
		setMask('*');
	}

	@Override
	public void setEditable(boolean editable) {
		super.setReadOnly(!editable);
	}

	@Override
	public char[] getValue() {
		String text = super.getText();
		if (text.length() == 0) return null;
		return text.toCharArray();
	}

	@Override
	public void setValue(char[] value) {
		String text = value != null ? new String(value) : "";
		textOnFocusLost = text;
		if (!isFocused()) {
			super.setText(text);
		}
	}
	
	private void fireChangeEvent() {
		textOnFocusLost = super.getText();
		changeListener.changed(LanternaPasswordField.this);
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
