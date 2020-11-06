package org.minimalj.model.validation;

import java.util.Collections;
import java.util.List;

public interface Validation {
	public static final List<ValidationMessage> EMPTY_MESSAGE_LIST = Collections.emptyList();
	
	/**
	 * 
	 * @return List of message or <code>null</code>
	 */
	public List<ValidationMessage> validate();

	/**
	 * Helper method to create a validation result containing exactly one validation message.
	 * 
	 * @param key the property causing the validation message
	 * @param formattedText the text to display
	 * @return a singleton list containing the specified key and text
	 */
	public static List<ValidationMessage> message(Object key, String formattedText) {
		ValidationMessage message = new ValidationMessage(key, formattedText);
		return Collections.singletonList(message);
	}
	
	/**
	 * Helper method to avoid NPE because of validate() returning null.
	 * 
	 * @return never returns <code>null</code>.
	 */
	public default List<ValidationMessage> validateNullSafe() {
		List<ValidationMessage> validationMessages = validate();
		if (validationMessages != null) {
			return validationMessages;
		} else {
			return EMPTY_MESSAGE_LIST;
		}
	}
 
	/**
	 * @param value primitive value to be checked
	 * @return true if the value is not null and not a object created by a
	 *         createInvalid method
	 */
	public default boolean isValid(Object value) {
		return value != null && !InvalidValues.isInvalid(value);
	}
	
}
