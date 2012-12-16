package ch.openech.mj.edit.validation;

import java.util.List;


public interface Validation {

	void validate(List<ValidationMessage> validationResult);
	
}
