package org.minimalj.frontend.edit;

import java.util.List;

import org.minimalj.model.validation.ValidationMessage;

public interface Indicator {

	public void setValidationMessages(List<ValidationMessage> validationMessages);
	
}
