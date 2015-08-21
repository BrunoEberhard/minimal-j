package org.minimalj.frontend.json;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.PasswordField;

public class JsonPasswordField extends JsonInputComponent<Object> implements PasswordField {
	private static final String MAX_LENGTH = "maxLength";
	
	public JsonPasswordField(int maxLength, InputComponentListener changeListener) {
		super("PasswordField", changeListener);
		put(MAX_LENGTH, maxLength);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void setValue(Object value) {
		if (value == null || value instanceof String) {
			put(VALUE, value);
		} else {
			throw new IllegalStateException(value.toString());
		}
	}

	@Override
	public String getValue() {
		return (String) get(VALUE);
	}

	@Override
	public void setEditable(boolean editable) {
		// ignored
	}
}
