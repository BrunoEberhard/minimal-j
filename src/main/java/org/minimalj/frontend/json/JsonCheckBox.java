package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.CheckBox;
import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;

public class JsonCheckBox extends JsonInputComponent implements CheckBox {

	public JsonCheckBox(String text, InputComponentListener changeListener) {
		super("CheckBox", changeListener);
		put("text", text);
	}

	@Override
	public void setSelected(boolean selected) {
		setValue(Boolean.toString(selected));
	}

	@Override
	public boolean isSelected() {
		return Boolean.TRUE.toString().equals(getValue());
	}

	@Override
	public void setEditable(boolean editable) {
		super.put(EDITABLE, editable);
		
	}
}
