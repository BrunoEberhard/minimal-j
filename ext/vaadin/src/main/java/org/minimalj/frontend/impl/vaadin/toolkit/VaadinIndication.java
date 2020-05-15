package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import com.vaadin.flow.component.HasValidation;

public class VaadinIndication {

	public static void setValidationMessages(List<String> validationMessages, HasValidation component) {
		if (validationMessages.isEmpty()) {
			component.setInvalid(false);
		} else if (validationMessages.size() == 1) {
			component.setInvalid(true);
			component.setErrorMessage(validationMessages.get(0));
		} else {
			component.setInvalid(true);
			StringBuilder b = new StringBuilder();
			for (int i = 0; i<validationMessages.size(); i++) {
				b.append(validationMessages.get(i)).append('\n');
			}
			component.setErrorMessage(b.toString());
		}
	}

}
