package ch.openech.mj.vaadin.toolkit;

import java.util.List;

import ch.openech.mj.edit.validation.ValidationMessage;

import com.vaadin.terminal.CompositeErrorMessage;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractComponent;

public class VaadinIndication {

	public static void setValidationMessages(List<ValidationMessage> validationMessages, AbstractComponent component) {
		if (validationMessages.isEmpty()) {
			component.setComponentError(null);
		} else if (validationMessages.size() == 1) {
			component.setComponentError(new UserError(validationMessages.get(0).getFormattedText()));
		} else {
			ErrorMessage[] errorMessages = new ErrorMessage[validationMessages.size()];
			for (int i = 0; i<validationMessages.size(); i++) {
				errorMessages[i] = new UserError(validationMessages.get(i).getFormattedText());
			}
			CompositeErrorMessage compositeErrorMessage = new CompositeErrorMessage(errorMessages);
			component.setComponentError(compositeErrorMessage);
		}
	}
		
}
