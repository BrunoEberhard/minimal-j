package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasCaption;

import com.vaadin.flow.component.checkbox.Checkbox;

public class VaadinCheckBox extends Checkbox implements Input<Boolean>, HasCaption {
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
