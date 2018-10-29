package org.minimalj.frontend.editor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class Validator {
	private static final Logger logger = Logger.getLogger(Validator.class.getName());

	public static void validate(Object object, List<ValidationMessage> validationMessages) {
		if (InvalidValues.isInvalid(object)) {
			String caption = Resources.getString(object.getClass());
			validationMessages.add(new ValidationMessage(null, MessageFormat.format(Resources.getString("ObjectValidator.message"), caption)));
		} else if (object instanceof Collection) {
			Collection<?> list = (Collection<?>) object;
			list.stream().forEach(o -> validate(o, validationMessages));
		} else if (object != null) {
			Collection<PropertyInterface> valueProperties = Properties.getProperties(object.getClass()).values();
			validate(object, validationMessages, valueProperties);
		}
	}

	public static void validate(Object object, List<ValidationMessage> validationMessages, Collection<PropertyInterface> properties) {
		properties.stream().filter(property -> {
			if (property instanceof ChainedProperty) {
				return ((ChainedProperty) property).isAvailableFor(object);
			} else {
				return true;
			}
		}).forEach(property -> {
			Object value = property.getValue(object);

			validateEmpty(validationMessages, value, property);
			validateSize(validationMessages, value, property);
			validateInvalid(validationMessages, value, property);

			if (value instanceof Validation) {
				Validation validation = (Validation) value;
				validationMessages.addAll(validation.validateNullSafe());
			}

			List<ValidationMessage> innerMessages = new ArrayList<>();
			validate(value, innerMessages);
			innerMessages.stream().forEach(m -> validationMessages.add(new ValidationMessage(property, m.getFormattedText())));
		});
	}

	private static void validateEmpty(List<ValidationMessage> resultList, Object value, PropertyInterface property) {
		if (property.getAnnotation(NotEmpty.class) != null && EmptyObjects.isEmpty(value)) {
			String caption = Resources.getPropertyName(property);
			String message;
			if (StringUtils.isEmpty(caption)) {
				message = Resources.getString("EmptyValidator.messageNoCaption");
			} else {
				message = MessageFormat.format(Resources.getString("EmptyValidator.message"), caption);
			}
			resultList.add(new ValidationMessage(property, message));
		}
	}

	private static void validateSize(List<ValidationMessage> validationMessages, Object value, PropertyInterface property) {
		if (value instanceof String) {
			String string = (String) value;
			int maxSize = AnnotationUtil.getSize(property, AnnotationUtil.OPTIONAL);
			if (string.length() > maxSize) {
				String caption = Resources.getPropertyName(property);
				validationMessages.add(new ValidationMessage(property, MessageFormat.format(Resources.getString("SizeValidator.message"), caption)));
			}
		}
	}

	private static void validateInvalid(List<ValidationMessage> validationMessages, Object value, PropertyInterface property) {
		if (InvalidValues.isInvalid(value)) {
			String message = InvalidValues.getMessage(value);
			if (message != null) {
				validationMessages.add(new ValidationMessage(property, message));
			} else {
				String caption = Resources.getPropertyName(property);
				validationMessages.add(new ValidationMessage(property, MessageFormat.format(Resources.getString("ObjectValidator.message"), caption)));
			}
		}
	}

	public static boolean allUsedFieldsValid(List<ValidationMessage> validationMessages, Collection<PropertyInterface> properties,
			boolean showWarningIfValidationForUnsuedElement) {
		for (ValidationMessage validationMessage : validationMessages) {
			if (properties.contains(validationMessage.getProperty())) {
				return false;
			} else {
				if (showWarningIfValidationForUnsuedElement) {
					logger.warning(
							"There is a validation message for " + validationMessage.getProperty().getName() + " but the element is not used in the form");
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
