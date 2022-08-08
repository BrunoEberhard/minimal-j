package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.model.validation.ValidationMessage;

public interface Indication {

	public void setValidationMessages(List<ValidationMessage> validationMessages, FormContent formContent);
	
}
