package org.minimalj.frontend.impl.json;

import java.util.List;
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
	public void changedValue(Object value) {
		char[] chars = null;
		if (value instanceof List) {
			List<String> charList = (List<String>) value;
			chars = new char[charList.size()];
			for (int i = 0; i<charList.size(); i++) {
				chars[i] = charList.get(i).charAt(0);
			}
		} 
		Object oldValue = super.putSilent(VALUE, chars);
		if (!Objects.equals(oldValue, chars)) {
			changeListener.changed(this);
		}
	}
	
	@Override
	public void setEditable(boolean editable) {
		// ignored
	}
}
