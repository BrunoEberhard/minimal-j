package org.minimalj.frontend.impl.swing.toolkit;

import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.PasswordField;
import org.minimalj.frontend.action.Action;

public class SwingPasswordField extends JPasswordField implements PasswordField {
	private static final long serialVersionUID = 1L;

	public SwingPasswordField(InputComponentListener changeListener, int maxLength) {
		addActionListener(e -> {
			SwingDialog dialog = SwingTextField.findDialog(this);
			if (dialog != null) {
				Action saveAction = dialog.getSaveAction();
				if (saveAction.isEnabled()) {
					SwingFrontend.run(e, saveAction::action);
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

	@Override
	/*
	 * the password is not stored in the model object to prevent security problems
	 */
	public void setValue(char[] value) {
		// ignored
	}

	@Override
	public char[] getValue() {
		return getPassword();
	}

}