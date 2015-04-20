package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;

public class JsonCheckBox extends JsonInputComponent<Boolean> {

	public JsonCheckBox(String text, InputComponentListener changeListener) {
		super("CheckBox", changeListener);
		put("text", text);
	}

	@Override
	public void setValue(Boolean selected) {
		put(VALUE, Boolean.toString(selected));
	}

	@Override
	public Boolean getValue() {
		return Boolean.TRUE.toString().equals(get(VALUE));
	}
}
