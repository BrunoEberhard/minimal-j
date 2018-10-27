package org.minimalj.frontend.impl.javafx.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import javafx.beans.value.ChangeListener;

public class FxCheckbox extends javafx.scene.control.CheckBox implements Input<Boolean> {

	public FxCheckbox(String text, InputComponentListener changeListener) {
		setText(text);
		selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> changeListener.changed(FxCheckbox.this));
	}

	@Override
	public Boolean getValue() {
		return Boolean.valueOf(super.isSelected());
	}

	@Override
	public void setValue(Boolean value) {
		setSelected(Boolean.TRUE.equals(value));
	}

	@Override
	public void setEditable(boolean editable) {
		// TODO
	}
}
