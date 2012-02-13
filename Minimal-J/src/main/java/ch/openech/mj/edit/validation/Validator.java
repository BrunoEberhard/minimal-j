package ch.openech.mj.edit.validation;

import java.util.List;

public interface Validator<T> {

	void validate(T object, List<ValidationMessage> resultList);
	
}
