package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasCaption;

import com.vaadin.flow.component.textfield.TextField;

public class VaadinReadOnlyTextField extends TextField implements Input<String>, HasCaption {
	private static final long serialVersionUID = 1L;
	
	public VaadinReadOnlyTextField() {
        setReadonly(true);
	}

	@Override
	public void setEditable(boolean editable) {
        // read only field cannot be editable
	}
}
