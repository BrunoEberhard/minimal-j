package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;

public abstract class JsonValueComponent extends JsonComponent {

	public static final String VALUE = "value";
	public static final String EDITABLE = "editable";

	private final InputComponentListener changeListener;
	
	public JsonValueComponent(String type, InputComponentListener changeListener) {
		super(type);
		this.changeListener = changeListener;
		if (changeListener == null) {
			put(EDITABLE, false);
		}
	}
	
	@Override
	public void put(String key, Object value) {
		super.put(key, value);
		if (changeListener != null) {
			changeListener.changed(this);
		}
	}

	public void setValue(String value) {
		put(VALUE, value);
	}

	public String getValue() {
		return (String) get(VALUE);
	}

}