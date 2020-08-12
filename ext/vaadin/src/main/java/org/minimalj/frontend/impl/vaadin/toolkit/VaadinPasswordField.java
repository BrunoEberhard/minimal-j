package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.PasswordField;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasCaption;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasComponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.KeyPressEvent;
import com.vaadin.flow.component.KeyUpEvent;
import com.vaadin.flow.shared.Registration;

/**
 * Cannot extends com.vaadin.flow.component.textfield.PasswordField because of
 * clash in getValue() method.
 *
 */
public class VaadinPasswordField implements PasswordField, KeyNotifier, HasCaption, HasComponent {
	private static final long serialVersionUID = 1L;

	private ValueChangeEvent<String> event;
	private final com.vaadin.flow.component.textfield.PasswordField field;
	
	public VaadinPasswordField(InputComponentListener changeListener, int maxLength) {
		field = new com.vaadin.flow.component.textfield.PasswordField();
		field.setMaxLength(maxLength);
		if (changeListener != null) {
			field.addValueChangeListener(new VaadinTextFieldTextChangeListener(changeListener));
		} else {
			setEditable(false);
		}
		field.setWidthFull();
	}

	private class VaadinTextFieldTextChangeListener implements ValueChangeListener<ValueChangeEvent<String>> {
		private static final long serialVersionUID = 1L;
		private final InputComponentListener changeListener;
		
		public VaadinTextFieldTextChangeListener(InputComponentListener changeListener) {
			this.changeListener = changeListener;
		}

		@Override
		public void valueChanged(ValueChangeEvent<String> event) {
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

    @Override
    public void setLabel(String label) {
        field.setLabel(label);
    }

    @Override
	public Registration addKeyDownListener(ComponentEventListener<KeyDownEvent> listener) {
		return field.addKeyDownListener(listener);
	}

    @Override
	public Registration addKeyPressListener(ComponentEventListener<KeyPressEvent> listener) {
		return field.addKeyPressListener(listener);
	}

    @Override
	public Registration addKeyUpListener(ComponentEventListener<KeyUpEvent> listener) {
		return field.addKeyUpListener(listener);
	}

    @Override
	public Registration addKeyDownListener(Key key, ComponentEventListener<KeyDownEvent> listener, KeyModifier... modifiers) {
		return field.addKeyDownListener(key, listener, modifiers);
	}

    @Override
	public Registration addKeyPressListener(Key key, ComponentEventListener<KeyPressEvent> listener, KeyModifier... modifiers) {
		return field.addKeyPressListener(key, listener, modifiers);
	}

    @Override
	public Registration addKeyUpListener(Key key, ComponentEventListener<KeyUpEvent> listener, KeyModifier... modifiers) {
		return field.addKeyUpListener(key, listener, modifiers);
	}

    @Override
    public Component getComponent() {
        return field;
    }

}
