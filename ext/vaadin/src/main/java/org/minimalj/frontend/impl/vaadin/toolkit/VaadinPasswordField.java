package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.PasswordField;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.HorizontalLayout;

/**
 * 
 * @author Bruno
 *
 */
public class VaadinPasswordField extends HorizontalLayout implements PasswordField {
	private static final long serialVersionUID = 1L;

	private TextChangeEvent event;
	private final com.vaadin.ui.PasswordField field;
	
	public VaadinPasswordField(InputComponentListener changeListener, int maxLength) {
		super();
		field = new com.vaadin.ui.PasswordField();
		field.setMaxLength(maxLength);
		field.setNullRepresentation("");
		field.setImmediate(true);
		if (changeListener != null) {
			field.addTextChangeListener(new VaadinTextFieldTextChangeListener(changeListener));
			field.addShortcutListener(new VaadinTextField.CommitShortcutListener());
		} else {
			setReadOnly(true);
		}
		addComponent(field);
		field.setWidth(100, Unit.PERCENTAGE);
	}

	private class VaadinTextFieldTextChangeListener implements TextChangeListener {
		private static final long serialVersionUID = 1L;
		private final InputComponentListener changeListener;
		
		public VaadinTextFieldTextChangeListener(InputComponentListener changeListener) {
			this.changeListener = changeListener;
		}

		@Override
		public void textChange(TextChangeEvent event) {
			VaadinPasswordField.this.event = event;
			changeListener.changed(VaadinPasswordField.this);
			VaadinPasswordField.this.event = null;
		}
	}

	@Override
	public void setEditable(boolean editable) {
		field.setReadOnly(!editable);
	}

	@Override
	public void setValue(char[] text) {
		// ignored
	}

	@Override
	public char[] getValue() {
		if (event != null) {
			return event.getText().toCharArray();
		} else {
			return field.getValue().toCharArray();
		}
	}

}
