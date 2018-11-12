package org.minimalj.frontend.editor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.minimalj.util.FieldUtils;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Framework internal. To do validation use the Annotations Size or NotEmpty or
 * implement Validation interface on your business entities or override validate
 * in the Editor.
 */
public class Validator {
	private static final Logger logger = Logger.getLogger(Validator.class.getName());

	public static List<ValidationMessage> validate(Object object) {
		if (object instanceof Collection) {
			Collection<?> list = (Collection<?>) object;
			// TODO java 8
			// list.stream().flatMap(Validator::validate).toList(Collectors.toList());
			List<ValidationMessage> messages = new ArrayList<>();
			for (Object element : list) {
				if (InvalidValues.isInvalid(element)) {
					messages.add(new ValidationMessage(null, Resources.getString("ObjectValidator.message")));
				} else {
					messages.addAll(validate(element));
				}
			}
			return messages;
		} else if (object != null && !FieldUtils.isAllowedPrimitive(object.getClass())) {
			Collection<PropertyInterface> valueProperties = Properties.getProperties(object.getClass()).values();
			List<ValidationMessage> validationMessages = new ArrayList<>();
			valueProperties.stream().forEach(property -> {
				Object value = property.getValue(object);

				validateEmpty(validationMessages, value, property);
				validateSize(validationMessages, value, property);
				validateInvalid(validationMessages, value, property);

				List<ValidationMessage> innerMessages = validate(value);
				innerMessages.forEach(m -> validationMessages.add(new ValidationMessage(chain(property, m.getProperty()), m.getFormattedText())));
			});
			if (object instanceof Validation) {
				Validation validation = (Validation) object;
				validationMessages.addAll(validation.validateNullSafe());
			}
			return validationMessages;
		} else {
			return Collections.emptyList();
		}
	}

	private static PropertyInterface chain(PropertyInterface p1, PropertyInterface p2) {
		if (p2 == null) {
			return p1;
		} else {
			return new ChainedProperty(p1, p2);
		}
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
