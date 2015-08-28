package org.minimalj.frontend.editor;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.EmptyValidator;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.model.validation.Validatable;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.resources.Resources;


public class ObjectValidator {

	private static final Logger logger = Logger.getLogger(ObjectValidator.class.getName());
	
	public static void validateForEmpty(Object object, List<ValidationMessage> validationMessages, Collection<PropertyInterface> properties) {
		for (PropertyInterface property : properties) {
			if (property.getAnnotation(NotEmpty.class) != null) {
				EmptyValidator.validate(validationMessages, object, property);
			}
		}
	}

	public static void validateForInvalid(Object object, List<ValidationMessage> validationMessages, Collection<PropertyInterface> properties) {
		for (PropertyInterface property : properties) {
			Object value = property.getValue(object);
			if (InvalidValues.isInvalid(value)) {
				String caption = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
				validationMessages.add(new ValidationMessage(property, caption + " ungültig"));
			}
		}
	}

	public static void validatePropertyValues(Object object, List<ValidationMessage> validationMessages, Collection<PropertyInterface> properties) {
		for (PropertyInterface property : properties) {
			Object value = property.getValue(object);
			if (value instanceof Validatable) {
				String validationMessage = ((Validatable) value).validate();
				if (validationMessage != null) {
					validationMessages.add(new ValidationMessage(property, validationMessage));
				}
			}
		}
	}

	public static boolean allUsedFieldsValid(List<ValidationMessage> validationMessages, Collection<PropertyInterface> properties, boolean showWarningIfValidationForUnsuedElement) {
		for (ValidationMessage validationMessage : validationMessages) {
			if (properties.contains(validationMessage.getProperty())) {
				return false;
			} else {
				if (showWarningIfValidationForUnsuedElement) {
					logger.warning("There is a validation message for " + validationMessage.getProperty().getName() + " but the element is not used in the form");
					logger.warning("The message is: " + validationMessage.getFormattedText());
					logger.fine("This can be ok if at some point not all validations in a object have to be ok");
					logger.fine("But you have to make sure to get valid data in database");
					logger.fine("You can avoid these warnings if you set showWarningIfValidationForUnsuedField to false");
				}
			}
		}
		return true;
	}
	
}
