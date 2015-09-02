package org.minimalj.frontend.impl.json;

import java.util.Objects;

import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.PasswordField;

public class JsonPasswordField extends JsonInputComponent<char[]> implements PasswordField {
	private static final String MAX_LENGTH = "maxLength";
	
	private InputComponentListener changeListener;
	
	public JsonPasswordField(int maxLength, InputComponentListener changeListener) {
		super("PasswordField", changeListener);
		put(MAX_LENGTH, maxLength);
		this.changeListener = changeListener;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void setValue(char[] value) {
		put(VALUE, value);
	}

	@Override
	public char[] getValue() {
		return (char[]) get(VALUE);
	}
	
	@Override
	public void changedValue(String value) {
		char[] chars = value != null ? value.toCharArray() : null;
		Object oldValue = super.put(VALUE, chars);
		if (!Objects.equals(oldValue, chars)) {
			changeListener.changed(this);
		}
	}
	
	@Override
	public void setEditable(boolean editable) {
		// ignored
	}
}
