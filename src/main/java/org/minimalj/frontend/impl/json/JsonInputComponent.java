package org.minimalj.frontend.impl.json;

import java.util.Objects;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Tooltip;

public abstract class JsonInputComponent<T> extends JsonComponent implements Input<T>, Tooltip {
	public static final String VALUE = "value";
	public static final String EDITABLE = "editable";
	public static final String TOOLTIP = "tooltip";

	private final InputComponentListener changeListener;
	
	public JsonInputComponent(String type, InputComponentListener changeListener) {
		super(type);
		this.changeListener = changeListener;
	}
	
	@Override
	public void setEditable(boolean editable) {
		put(EDITABLE, editable);
	}

	@Override
	public void setTooltip(String tooltip) {
		put(TOOLTIP, tooltip);
	}
	
	/*
	 * Should only be called if user has changed the value. The putSilent avoids
	 * the send back. Send back could conflict with the next user change (for example
	 * if user types very fast in a text field or password field)
	 */
	public void changedValue(Object value) {
		Object oldValue = super.putSilent(VALUE, value);
		if (!Objects.equals(oldValue, value)) {
			fireChange();
		}
	}

	void fireChange() {
		changeListener.changed(this);
	}
}