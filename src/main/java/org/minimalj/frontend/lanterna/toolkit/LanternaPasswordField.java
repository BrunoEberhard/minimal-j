package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.PasswordField;
import org.minimalj.util.StringUtils;

import com.googlecode.lanterna.gui.component.InteractableComponent;
import com.googlecode.lanterna.gui.component.PasswordBox;
import com.googlecode.lanterna.gui.listener.ComponentAdapter;
import com.googlecode.lanterna.input.Key;

public class LanternaPasswordField extends PasswordBox implements PasswordField {

	private final InputComponentListener changeListener;
	
	private String textOnFocusLost;

	public LanternaPasswordField(InputComponentListener changeListener) {
		this.changeListener = changeListener;
		addComponentListener(new TextFieldComponentListener());
	}

	@Override
	public void setEditable(boolean editable) {
		super.setVisible(editable);
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
		if (!hasFocus()) {
			super.setText(text);
		}
	}
		
	private void fireChangeEvent() {
		changeListener.changed(LanternaPasswordField.this);
	}
	
	@Override
	public Result keyboardInteraction(Key key) {
		Result result = super.keyboardInteraction(key);
		if (result != Result.EVENT_NOT_HANDLED) {
			fireChangeEvent();
		}
		return result;
	}

	private class TextFieldComponentListener extends ComponentAdapter {

		@Override
		public void onComponentReceivedFocus(InteractableComponent interactableComponent) {
			textOnFocusLost = getText();
		}
		
		@Override
		public void onComponentLostFocus(InteractableComponent interactableComponent) {
			if (!StringUtils.equals(textOnFocusLost, getText())) {
				setText(textOnFocusLost);
			}
		}
	}
	
}
