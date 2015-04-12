package org.minimalj.frontend.json;

import java.util.Objects;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;

public abstract class JsonValueComponent extends JsonComponent {
	private static final long serialVersionUID = 1L;
	
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
	public Object put(String property, Object value) {
		Object oldValue = super.put(property, value);
		if (changeListener != null && !Objects.equals(oldValue, value)) {
			changeListener.changed(this);
			JsonClientToolkit.getSession().propertyChange(getId(), property, value);
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