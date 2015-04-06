package org.minimalj.frontend.json;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.ComboBox;

public class JsonCombobox<T> extends JsonValueComponent implements ComboBox<T> {

	public JsonCombobox(List<T> objects, InputComponentListener changeListener) {
		super("Combobox", changeListener);
		
	}

	@Override
	public void setSelectedObject(T object) {
	}

	@Override
	public T getSelectedObject() {
		return null;
	}

	@Override
	public void setEditable(boolean editable) {
		put(EDITABLE, editable);
	}
}
