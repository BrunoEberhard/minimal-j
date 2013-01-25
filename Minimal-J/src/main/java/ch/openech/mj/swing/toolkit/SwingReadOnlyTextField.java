package ch.openech.mj.swing.toolkit;

import java.awt.event.FocusListener;

import javax.swing.JLabel;

import ch.openech.mj.toolkit.TextField;

public class SwingReadOnlyTextField extends JLabel implements TextField {

	public SwingReadOnlyTextField() {
	}

	@Override
	public void requestFocus() {
		// TODO Auto-generated method stub
		
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
	public void setEnabled(boolean editable) {
		
	}

	@Override
	public void setFocusListener(FocusListener focusListener) {
		// TODO Auto-generated method stub
		
	}
	
}

