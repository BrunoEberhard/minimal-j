package ch.openech.mj.swing.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import ch.openech.mj.toolkit.TextField;

public class SwingTextField extends JTextField implements TextField {
	private final ChangeListener changeListener;
	private FocusListener focusListener;
	private Runnable commitListener;
	
	public SwingTextField(ChangeListener changeListener, int maxLength) {
		this(changeListener, maxLength, null);
	}
	
	public SwingTextField(ChangeListener changeListener, int maxLength, String allowedCharacters) {
		super(new FilteredDocument(maxLength, allowedCharacters), null, 0);
		
		this.changeListener = changeListener;
		getDocument().addDocumentListener(new TextFieldChangeListener());
		
		setInheritsPopupMenu(true);
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (commitListener != null) {
					commitListener.run();
				}
			}
		});
	}

	public class TextFieldChangeListener implements DocumentListener {

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
			changeListener.stateChanged(new ChangeEvent(SwingTextField.this));
		}
	}

	private static class FilteredDocument extends PlainDocument {
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
	public void setFocusListener(FocusListener focusListener) {
		if (this.focusListener != null) {
			removeFocusListener(this.focusListener);
		}
		this.focusListener = focusListener;
		if (this.focusListener != null) {
			addFocusListener(this.focusListener);
		}
	}

	@Override
	public void setCommitListener(Runnable commitListener) {
		this.commitListener = commitListener;
	}
	
}

