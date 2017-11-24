package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.googlecode.lanterna.gui2.CheckBox;

public class LanternaCheckBox extends CheckBox implements Input<Boolean> {

	private final InputComponentListener changeListener;
	
	public LanternaCheckBox(InputComponentListener changeListener, String label) {
		super(label);
		this.changeListener = changeListener;
		addListener(checked -> fireChangeEvent());
	}

	@Override
	public void setValue(Boolean selected) {
		setChecked(Boolean.TRUE.equals(selected));
	}

	@Override
	public Boolean getValue() {
		return super.isChecked();
	}

	private void fireChangeEvent() {
		changeListener.changed(LanternaCheckBox.this);
	}
	
	@Override
	public void setEditable(boolean enabled) {
		// not supported
	}
}