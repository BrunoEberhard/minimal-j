package org.minimalj.frontend.editor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.EmptyValidator;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.resources.Resources;

public class ObjectValidator {
	private static final Logger logger = Logger.getLogger(ObjectValidator.class.getName());
	
	public static void validate(Object object, List<ValidationMessage> validationMessages, Collection<PropertyInterface> properties) {
		properties = filterAvailableProperties(object, properties);
		ObjectValidator.validateForEmpty(object, validationMessages, properties);
		ObjectValidator.validateForInvalid(object, validationMessages, properties);
		ObjectValidator.validatePropertyValues(object, validationMessages, properties);
	}

	/*
	public static Collection<PropertyInterface> not_working_in_cheerpj_filterAvailableProperties(Object object, Collection<PropertyInterface> properties) {
		return properties.stream().filter((PropertyInterface property) -> {
			if (property instanceof ChainedProperty) {
				return ((ChainedProperty) property).isAvailableFor(object);
			} else {
				return true;
			}
		}).collect(Collectors.toList());
		
		// Caused by: java.lang.IllegalAccessException: member is private to package: java.util.ArrayList.<init>()void/newInvokeSpecial, from java.util.stream.Collectors
	}
	*/
	
	public static Collection<PropertyInterface> filterAvailableProperties(Object object, Collection<PropertyInterface> properties) {
		List<PropertyInterface> result = new ArrayList<>();
		for (PropertyInterface p : properties) {
			if (p instanceof ChainedProperty) {
				if (((ChainedProperty) p).isAvailableFor(object)) {
					result.add(p);
				}
			} else {
				result.add(p);
			}
		}
		return result;
	}

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
				String caption = Resources.getPropertyName(property);
				validationMessages.add(new ValidationMessage(property, MessageFormat.format(Resources.getString("ObjectValidator.message"), caption)));
			}
		}
	}

	public static void validatePropertyValues(Object object, List<ValidationMessage> validationMessages, Collection<PropertyInterface> properties) {
		for (PropertyInterface property : properties) {
			Object value = property.getValue(object);
			if (value instanceof Validation) {
				List<ValidationMessage> thisMessages = ((Validation) value).validateNullSafe();
				for (ValidationMessage m : thisMessages) {
					validationMessages.add(new ValidationMessage(property, m.getFormattedText()));
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
