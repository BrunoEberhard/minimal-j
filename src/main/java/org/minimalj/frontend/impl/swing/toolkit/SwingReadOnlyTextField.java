package org.minimalj.frontend.impl.swing.toolkit;

import javax.swing.JLabel;
import javax.swing.UIManager;

import org.minimalj.frontend.Frontend.Input;

public class SwingReadOnlyTextField extends JLabel implements Input<String> {
	private static final long serialVersionUID = 1L;

	public SwingReadOnlyTextField() {
	}

	@Override
	public void updateUI() {
		super.updateUI();
		setBackground(UIManager.getColor("TextField.background"));
		setOpaque(true);
	}

	@Override
	public void setValue(String text) {
		super.setText(text);
	}

	@Override
	public String getValue() {
		return super.getText();
	}

	@Override
	public void setEditable(boolean editable) {
		// read only field cannot be enabled
	}

	@Override
	public void requestFocus() {
		// read only field cannot be focused
	}
}

