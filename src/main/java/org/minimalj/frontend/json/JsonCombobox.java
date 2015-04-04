package org.minimalj.frontend.json;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.ComboBox;

public class JsonCombobox<T> extends JsonComponent implements ComboBox<T> {

	private static final String EDITABLE = "editable";
	
	public JsonCombobox(List<T> objects, InputComponentListener changeListener) {
		super("combobox");
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
