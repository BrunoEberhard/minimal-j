package ch.openech.mj.edit.validation;

import java.util.List;

public interface Validatable {

	void validate(List<ValidationMessage> resultList);
	
}
