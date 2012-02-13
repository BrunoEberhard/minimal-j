package ch.openech.mj.swing.toolkit;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//should be obsolet with the new Toolkit-idea
@Deprecated
public class TextFieldChangeListener implements DocumentListener {

	private JTextField textField;
	private Object source;
	private ChangeListener changeListener;
	
	public TextFieldChangeListener(JTextField textField, Object source) {
		this.source = source;
		this.textField = textField;
	}

	public void setChangeListener(ChangeListener changeListener) {
		if (changeListener == null) {
			if (this.changeListener != null) {
				textField.getDocument().removeDocumentListener(this);
			}
		} else {
			if (this.changeListener == null) {
				textField.getDocument().addDocumentListener(this);
			}
		}		
		this.changeListener = changeListener;
	}
	
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		fireChangeEvent();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		fireChangeEvent();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		fireChangeEvent();
	}
	
	private void fireChangeEvent() {
		changeListener.stateChanged(new ChangeEvent(source));
	}
}
