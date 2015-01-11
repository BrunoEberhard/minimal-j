package org.minimalj.model.validation;

import java.util.List;


public interface Validation {

	void validate(List<ValidationMessage> validationResult);
	
}
