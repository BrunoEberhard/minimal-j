package org.minimalj.frontend.impl.javafx.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import javafx.beans.value.ChangeListener;

public class FxComboBox<T> extends javafx.scene.control.ComboBox<T> implements Input<T> {

	public FxComboBox(List<T> objects, InputComponentListener changeListener) {
		getItems().addAll(objects);
		valueProperty().addListener((ChangeListener<T>) (observable, oldValue, newValue) -> changeListener.changed(FxComboBox.this));
	}

}
