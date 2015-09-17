package org.minimalj.frontend.impl.json;

import java.util.Objects;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

public abstract class JsonInputComponent<T> extends JsonComponent implements Input<T> {
	private static final long serialVersionUID = 1L;
	
	public static final String VALUE = "value";
	public static final String EDITABLE = "editable";

	private final InputComponentListener changeListener;
	
	public JsonInputComponent(String type, InputComponentListener changeListener) {
		super(type);
		this.changeListener = changeListener;
	}
	
	@Override
	public void setEditable(boolean editable) {
		put(EDITABLE, editable);
	}

	public void changedValue(Object value) {
		Object oldValue = super.put(VALUE, value);
		if (!Objects.equals(oldValue, value)) {
			changeListener.changed(this);
		}
	}
}