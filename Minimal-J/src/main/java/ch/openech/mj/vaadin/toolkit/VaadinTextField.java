package ch.openech.mj.vaadin.toolkit;

import java.awt.event.FocusListener;

import ch.openech.mj.toolkit.ClientToolkit.InputComponentListener;
import ch.openech.mj.toolkit.TextField;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;

/**
 * 
 * @author Bruno
 *
 */
public class VaadinTextField extends com.vaadin.ui.TextField implements TextField {

	private TextChangeEvent event;
	
	public VaadinTextField(InputComponentListener changeListener, int maxLength) {
		this(changeListener, maxLength, null);
	}
	
	public VaadinTextField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		setMaxLength(maxLength);
		setNullRepresentation("");
		setImmediate(true);
		if (changeListener != null) {
			addListener(new VaadinTextFieldTextChangeListener(changeListener));
		} else {
			setReadOnly(true);
		}
	}

	@Override
	public void setText(String text) {
		setValue(text);
	}

	@Override
	public String getText() {
		if (event != null) {
			return event.getText();
		} else {
			return (String) getValue();
		}
	}

	@Override
	public void setFocusListener(FocusListener focusListener) {
		// TODO
	}

	@Override
	public void setCommitListener(Runnable runnable) {
		// TODO listening to Enter Key at Vaadin TextField
	}
	
	private class VaadinTextFieldTextChangeListener implements TextChangeListener {
		private final InputComponentListener changeListener;
		
		public VaadinTextFieldTextChangeListener(InputComponentListener changeListener) {
			this.changeListener = changeListener;
		}

		@Override
		public void textChange(TextChangeEvent event) {
			VaadinTextField.this.event = event;
			changeListener.changed(VaadinTextField.this);
			VaadinTextField.this.event = null;
		}
	}

	@Override
	public void setEditable(boolean editable) {
		setReadOnly(!editable);
	}
	
}
