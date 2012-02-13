package ch.openech.mj.swing.toolkit;

import java.awt.event.FocusListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.swing.component.IndicatingTextField;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.TextField;

public class SwingTextField extends IndicatingTextField implements TextField, Focusable {
	private TextFieldChangeListener changeListener;
	private FocusListener focusListener;
//	private KeyListener keyListener;

	public SwingTextField() {
	}

	public SwingTextField(int maxLength) {
		// TODO Eigenes Document verwenden, das effizienter ist, als das generelle FilteredDocument
		this(new LimitTextFieldFilter(maxLength));
	}
	
	public SwingTextField(TextFieldFilter filter) {
		super(new FilteredDocument(filter));
		((FilteredDocument) getDocument()).setTextField(this);
	}

	@Override
	public void setChangeListener(ChangeListener listener) {
		if (changeListener == null) {
			changeListener = new TextFieldChangeListener();
		}
		changeListener.setChangeListener(listener);
	}
	
	public class TextFieldChangeListener implements DocumentListener {

		private ChangeListener changeListener;
		
		public void setChangeListener(ChangeListener changeListener) {
			if (changeListener == null) {
				if (this.changeListener != null) {
					getDocument().removeDocumentListener(this);
				}
			} else {
				if (this.changeListener == null) {
					getDocument().addDocumentListener(this);
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
			} else {
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
		public String filter(Object textField, String str) {
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

