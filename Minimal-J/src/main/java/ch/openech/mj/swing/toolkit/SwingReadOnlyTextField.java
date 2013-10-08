package ch.openech.mj.swing.toolkit;

import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.UIManager;

import ch.openech.mj.toolkit.TextField;

public class SwingReadOnlyTextField extends JLabel implements TextField {
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
	public void setText(String text) {
		super.setText(text);
	}

	@Override
	public String getText() {
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

	@Override
	public void setFocusListener(FocusListener focusListener) {
		// read only field cannot be focused
	}

	@Override
	public void setCommitListener(Runnable runnable) {
		// read only field cannot get commit command
	}
	
}

