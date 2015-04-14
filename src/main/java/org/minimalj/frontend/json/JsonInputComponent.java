package org.minimalj.frontend.json;

import java.util.Objects;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;

public abstract class JsonInputComponent extends JsonComponent {
	private static final long serialVersionUID = 1L;
	
	public static final String VALUE = "value";
	public static final String EDITABLE = "editable";

	private final InputComponentListener changeListener;
	
	public JsonInputComponent(String type, InputComponentListener changeListener) {
		super(type);
		this.changeListener = changeListener;
	}
	
	@Override
	public Object put(String property, Object value) {
		Object oldValue = super.put(property, value);
		if (changeListener != null && !Objects.equals(oldValue, value)) {
			changeListener.changed(this);
		}
		return oldValue;
	}

	public void setValue(String value) {
		put(VALUE, value);
	}

	public String getValue() {
		return (String) get(VALUE);
	}

}