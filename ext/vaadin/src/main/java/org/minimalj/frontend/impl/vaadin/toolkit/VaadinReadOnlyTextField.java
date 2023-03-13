package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasCaption;

import com.vaadin.flow.component.textfield.TextArea;

public class VaadinReadOnlyTextField extends TextArea implements Input<String>, HasCaption {
	private static final long serialVersionUID = 1L;
	private String value;
	
	public VaadinReadOnlyTextField() {
		setReadOnly(true);
		value = getValue();
	}

	@Override
	public void setValue(String value) {
		this.value = value;
		super.setValue(value != null ? value : "");
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setEditable(boolean editable) {
        // read only field cannot be editable
	}
}
