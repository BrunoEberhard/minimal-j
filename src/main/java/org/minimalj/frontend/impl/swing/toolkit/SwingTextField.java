package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.util.StringUtils;

public class SwingTextField extends JTextField implements Input<String>, FocusListener {
	private static final long serialVersionUID = 1L;
	
	private final InputComponentListener changeListener;
	
	private String textOnFocusLost;
	
	public SwingTextField(InputComponentListener changeListener, int maxLength) {
		this(changeListener, maxLength, null);
	}
	
	public SwingTextField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		super(new FilteredDocument(maxLength, allowedCharacters), null, 0);
		
		this.changeListener = changeListener;
		getDocument().addDocumentListener(new TextFieldChangeListener());
		
		setInheritsPopupMenu(true);
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingInternalFrame frame = findFrame();
				if (frame != null) {
					Action saveAction = frame.getSaveAction();
					if (saveAction.isEnabled()) {
						saveAction.action();
					}
				}
			}
		});
		addFocusListener(this);
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

	public class TextFieldChangeListener implements DocumentListener, Runnable {
		private boolean invokeSet = false;
		
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
			// gather all remove/insert of document in one change
			if (!invokeSet) {
				invokeSet = true;
				SwingUtilities.invokeLater(this);
			}
		}

		@Override
		public void run() {
			invokeSet = false;
			textOnFocusLost = null;
			changeListener.changed(SwingTextField.this);
		}
	}

	static class FilteredDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		private final int maxLength;
		private final String allowedCharacters;
		
		public FilteredDocument(int maxLength, String allowedCharacters) {
			this.maxLength = maxLength;
			this.allowedCharacters = allowedCharacters;
		}

		@Override
		public void insertString(int offset, String additionalString, AttributeSet attr) throws BadLocationException {
			int length = getLength();
			String actualText = getText(0, length);

			String requestedString = actualText.substring(0, offset) + additionalString + actualText.substring(offset, length);

			String filteredAdditionalString = filter(additionalString);
			int overLength = actualText.length() + filteredAdditionalString.length() - maxLength;
			if (overLength > 0) {
				filteredAdditionalString = filteredAdditionalString.substring(0, filteredAdditionalString.length() - overLength);
			}
			String filteredString = actualText.substring(0, offset) + filteredAdditionalString + actualText.substring(offset, length);

			if (requestedString.equals(filteredString)) {
				super.insertString(offset, additionalString, attr);
			} else if (!actualText.equals(filteredString)) {
				replace(0, length, filteredString, attr);
			}
		}
		
		private String filter(String s) {
			if (allowedCharacters == null) return s;
			
			String result = "";
			for (int i = 0; i<s.length(); i++) {
				char c = s.charAt(i);
				if (allowedCharacters.indexOf(c) < 0) {
					if (Character.isLowerCase(c)) c = Character.toUpperCase(c);
					else if (Character.isUpperCase(c)) c = Character.toLowerCase(c);
					if (allowedCharacters.indexOf(c) < 0) {
						continue;
					}
				}
				result += c;
			}
			return result;
		}

	}

	@Override
	public void focusGained(FocusEvent e) {
		//	textOnFocusLost = getText();
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (textOnFocusLost != null) {
			super.setText(textOnFocusLost);
		}
	}
	
	@Override
	public void setValue(String value) {
		textOnFocusLost = value;
		if (!hasFocus() && !StringUtils.equals(value, getText())) {
			setText(value);
		}
	}

	@Override
	public String getValue() {
		return super.getText();
	}

}