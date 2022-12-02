package org.minimalj.model.validation;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

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
	 * Helper method to indicate a field having an invalid value
	 * 
	 * @param key the constant you get from the $ constant (or property)
	 * @return a ValidationMessage with formatted text
	 */
	public static ValidationMessage createInvalidValidationMessage(Object key) {
		Property property = Keys.getProperty(key);
		String caption = Resources.getPropertyName(property);
		return new ValidationMessage(property, MessageFormat.format(Resources.getString("ObjectValidator.message"), caption));
	}

	/**
	 * Helper method to indicate a mandatory field is empty
	 * 
	 * @param key the constant you get from the $ constant (or property)
	 * @return a ValidationMessage with formatted text
	 */
	public static ValidationMessage createEmptyValidationMessage(Object key) {
		Objects.requireNonNull(key);
		Property property = Keys.getProperty(key);
		String caption = Resources.getPropertyName(property);
		String message;
		if (StringUtils.isEmpty(caption)) {
			message = Resources.getString("EmptyValidator.messageNoCaption");
		} else {
			message = MessageFormat.format(Resources.getString("EmptyValidator.message"), caption);
		}
		return new ValidationMessage(property, message);
	}

	/**
	 * Can be used as shortcut by custom validation
	 * @param object the object containing the values
	 * @param messages the result list
	 * @param keys the keys of the fields that must not be empty
	 */
	public static void validateNotEmpty(Object object, List<ValidationMessage> messages, Object... keys) {
		for (Object key : keys) {
			Property property = Keys.getProperty(key);
			Object value = property.getValue(object);
			if (EmptyObjects.isEmpty(value)) {
				messages.add(Validation.createEmptyValidationMessage(key));
			}
		}
	}

	public static <T> void validate(T object, T key, Predicate<T> predicate, List<ValidationMessage> messages) {
		Property property = Keys.getProperty(key);
		T value = (T) property.getValue(object);
		if (predicate.test(value)) {
			messages.add(Validation.createInvalidValidationMessage(key));
		}
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
