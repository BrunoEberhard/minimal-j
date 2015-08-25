package org.minimalj.frontend.swing.toolkit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.PasswordField;
import org.minimalj.frontend.action.Action;

public class SwingPasswordField extends JPasswordField implements PasswordField {
	private static final long serialVersionUID = 1L;
	
	public SwingPasswordField(InputComponentListener changeListener, int maxLength) {
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingInternalFrame frame = findFrame();
				if (frame != null) {
					SwingFrontend.updateEventTab((Component) e.getSource());
					Action saveAction = frame.getSaveAction();
					saveAction.action();
				}
			}
		});
		
		getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				changeListener.changed(SwingPasswordField.this);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				changeListener.changed(SwingPasswordField.this);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				changeListener.changed(SwingPasswordField.this);
			}
		});
	}
	
	private SwingInternalFrame findFrame() {
		Component c = this;
		while (c != null) {
			if (c instanceof SwingInternalFrame) {
				return (SwingInternalFrame) c;
			}
			c = c.getParent();
		}
		return null;
	}

	// the password is not stored in the model object to
	// prevent security problems
	
//	@Override
//	public void setValue(String value) {
//		// ignored
//	}
//
//	@Override
//	public String getValue() {
//		// only show if the password field is empty to validate
//		if (getPassword().length > 0) {
//			return "x";
//		} else {
//			return "";
//		}
//	}
	
	@Override
	public void setValue(char[] value) {
		// ignored
	}

	@Override
	public char[] getValue() {
		return getPassword();
	}

}