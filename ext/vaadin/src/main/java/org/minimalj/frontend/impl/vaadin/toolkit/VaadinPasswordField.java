package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.PasswordField;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.ui.HorizontalLayout;

/**
 * 
 * @author Bruno
 *
 */
public class VaadinPasswordField extends HorizontalLayout implements PasswordField {
	private static final long serialVersionUID = 1L;

	private ValueChangeEvent<String> event;
	private final com.vaadin.ui.PasswordField field;
	
	public VaadinPasswordField(InputComponentListener changeListener, int maxLength) {
		setMargin(false);
		field = new com.vaadin.ui.PasswordField();
		field.setMaxLength(maxLength);
		if (changeListener != null) {
			field.addValueChangeListener(new VaadinTextFieldTextChangeListener(changeListener));
			field.addShortcutListener(new VaadinTextField.CommitShortcutListener());
		} else {
			setReadOnly(true);
		}
		addComponent(field);
		field.setWidth(100, Unit.PERCENTAGE);
	}

	private class VaadinTextFieldTextChangeListener implements ValueChangeListener<String> {
		private static final long serialVersionUID = 1L;
		private final InputComponentListener changeListener;
		
		public VaadinTextFieldTextChangeListener(InputComponentListener changeListener) {
			this.changeListener = changeListener;
		}

		@Override
		public void valueChange(ValueChangeEvent<String> event) {
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
			return event.getValue().toCharArray();
		} else {
			return field.getValue().toCharArray();
		}
	}

}
