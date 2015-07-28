package org.minimalj.frontend.lanterna.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.googlecode.lanterna.gui.component.CheckBox;
import com.googlecode.lanterna.input.Key;

public class LanternaCheckBox extends CheckBox implements Input<Boolean> {

	private final InputComponentListener changeListener;
	
	public LanternaCheckBox(InputComponentListener changeListener, String label) {
		super(label, false);
		this.changeListener = changeListener;
	}

	@Override
	public void setValue(Boolean selected) {
		if (isScrollable() != (Boolean.TRUE.equals(selected))) {
			select();
		}
	}

	@Override
	public Boolean getValue() {
		return isSelected();
	}

	private void fireChangeEvent() {
		changeListener.changed(LanternaCheckBox.this);
	}
	
	@Override
	public Result keyboardInteraction(Key key) {
		Result result = super.keyboardInteraction(key);
		if (result != Result.EVENT_NOT_HANDLED) {
			fireChangeEvent();
		}
		return result;
	}
	
	@Override
	public void setEditable(boolean enabled) {
		// not supported
	}
}