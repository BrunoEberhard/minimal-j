package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.vaadin.ui.CheckBox;

public class VaadinCheckBox extends CheckBox implements Input<Boolean> {
	private static final long serialVersionUID = 1L;
	
	public VaadinCheckBox(InputComponentListener listener, String text) {
		super(text);
		addValueChangeListener(event -> listener.changed(VaadinCheckBox.this));
	}

	@Override
	public void setEditable(boolean editable) {
		setReadOnly(!editable);
	}
	
}
