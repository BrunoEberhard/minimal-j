package org.minimalj.frontend.impl.swing.toolkit;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.UIManager;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.util.StringUtils;

public class SwingReadOnlyTextField extends JLabel implements Input<String> {
	private static final long serialVersionUID = 1L;

	public SwingReadOnlyTextField() {
	}

	@Override
	public void updateUI() {
		super.updateUI();
		setBackground(UIManager.getColor("TextField.background"));
		setBorder(BorderFactory.createLineBorder(UIManager.getColor("TextField.shadow"), 1));
		setOpaque(true);
	}

	@Override
	public void setValue(String string) {
		if (string != null) {
			if (string.contains("\n")) {
				string = StringUtils.escapeHTML(string);
				string = string.replaceAll("\n", "<br>");
				setText("<html><body>" + string + "</body></html>");
			} else {
				setText(string);
			}
		} else {
			setText(null);
		}
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

