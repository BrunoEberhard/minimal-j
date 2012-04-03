package ch.openech.mj.swing.toolkit;

import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;

public class SwingTextField extends JTextField implements TextField, Focusable {
	private final ChangeListener changeListener;
	private FocusListener focusListener;
//	private KeyListener keyListener;

	public SwingTextField() {
		super();
		this.changeListener = null;
		setEditable(false);
		setInheritsPopupMenu(true);
	}
	
	public SwingTextField(ChangeListener changeListener, int maxLength) {
		// TODO Eigenes Document verwenden, das effizienter ist, als das generelle FilteredDocument
		this(changeListener, new LimitTextFieldFilter(maxLength));
	}
	
	public SwingTextField(ChangeListener changeListener, TextFieldFilter filter) {
		super(new FilteredDocument(filter), null, 0);
		((FilteredDocument) getDocument()).setTextField(this);
		
		this.changeListener = changeListener;
		getDocument().addDocumentListener(new TextFieldChangeListener());
		
		setInheritsPopupMenu(true);
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
		private final TextFieldFilter filter;
		private SwingTextField textField;
		
		public FilteredDocument(TextFieldFilter filter) {
			this.filter = filter;
		}

		@Override
		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
			int length = getLength();
			String actualText = getText(0, length);
			String requestedString = actualText.substring(0, offset) + str + actualText.substring(offset, length);
			String filteredString = filter.filter(textField, requestedString);
			if (requestedString.equals(filteredString)) {
				super.insertString(offset, str, attr);
			} else if (!actualText.equals(filteredString)) {
				replace(0, length, filteredString, attr);
			}
		}

		public void setTextField(SwingTextField textField) {
			this.textField = textField;
		}

	}

	private static class LimitTextFieldFilter implements TextFieldFilter {
		private int maxLength;

		public LimitTextFieldFilter(int maxLength) {
			this.maxLength = maxLength;
		}

		@Override
		public String filter(IComponent textField, String str) {
			if (str == null)
				return null;
			
			if (str.length() <= maxLength) {
				return str;
			} else {
				ClientToolkit.getToolkit().showNotification(textField, "Eingabe auf " + maxLength + " Zeichen beschränkt");
				return str.substring(0, maxLength);
			}
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
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		SwingIndication.setValidationMessagesToCaption(validationMessages, this);
	}

//	@Override
//	public void setKeyListener(KeyListener keyListener) {
//		if (this.keyListener != null) {
//			removeKeyListener(this.keyListener);
//		}
//		this.keyListener = keyListener;
//		if (this.keyListener != null) {
//			addKeyListener(this.keyListener);
//		}
//	}
	
}

