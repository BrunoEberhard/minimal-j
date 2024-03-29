package org.minimalj.frontend.editor;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.minimalj.model.Selection;
import org.minimalj.model.View;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.model.properties.VirtualProperty;
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
		return validate(object, new HashSet<>());
	}

	private static List<ValidationMessage> validate(Object object, Set<Object> validated) {
		if (validated.contains(object)) {
			return Collections.emptyList();
		} else {
			validated.add(object);
		}
		if (object instanceof Collection) {
			Collection<?> list = (Collection<?>) object;
			List<ValidationMessage> messages = new ArrayList<>();
			int index = 0;
			for (Object element : list) {
				IndexProperty indexProperty = new IndexProperty(index);
				if (InvalidValues.isInvalid(element)) {
					messages.add(new ValidationMessage(null, Resources.getString("ObjectValidator.message")));
				} else {
					List<ValidationMessage> elementMessages = validate(element, validated);
					elementMessages.forEach(m -> messages.add(new ValidationMessage(chain(indexProperty, m.getProperty()), m.getFormattedText())));
				}
				index = index + 1;
			}
			return messages;
		} else if (object != null && !FieldUtils.isAllowedPrimitive(object.getClass()) && !(object instanceof Selection) && !(object instanceof View)) {
			Collection<Property> valueProperties = Properties.getProperties(object.getClass()).values();
			List<ValidationMessage> validationMessages = new ArrayList<>();
			valueProperties.stream().filter(p -> !StringUtils.equals(p.getName(), "id", "version", "historized")).forEach(property -> {
				Object value = property.getValue(object);

				validateEmpty(validationMessages, value, property);
				validateSize(validationMessages, value, property);
				validateInvalid(validationMessages, value, property);

				List<ValidationMessage> innerMessages = validate(value, validated);
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

	private static Property chain(Property p1, Property p2) {
		if (p2 == null) {
			return p1;
		} else {
			return new ChainedProperty(p1, p2);
		}
	}
	
	public static class IndexProperty extends VirtualProperty {
		private final int index;
		
		public IndexProperty(int index) {
			this.index = index;
		}
		
		public int getIndex() {
			return index;
		}
		
		@Override
		public String getName() {
			return "[" + index + "]";
		}
		
		@Override
		public Class<?> getDeclaringClass() {
			return List.class;
		}

		@Override
		public Class<?> getClazz() {
			return Integer.class;
		}

		@Override
		public Object getValue(Object object) {
			// not implemented
			return null;
		}

		@Override
		public void setValue(Object object, Object value) {
			// not implemented
		}
		
	}
	

	private static void validateEmpty(List<ValidationMessage> resultList, Object value, Property property) {
		NotEmpty notEmpty = property.getAnnotation(NotEmpty.class);
		if (notEmpty != null && EmptyObjects.isEmpty(value) && !(notEmpty.zeroAllowed() && isZero(value))) {
			resultList.add(Validation.createEmptyValidationMessage(property));
		}
	}
	
	private static boolean isZero(Object value) {
		if (value instanceof Integer) {
			return ((Integer) value) == 0;
		} else if (value instanceof Long) {
			return ((Long) value) == 0;
		} else if (value instanceof BigDecimal) {
			return ((BigDecimal) value).signum() == 0;
		} else {
			return false;
		}
	}

	private static void validateSize(List<ValidationMessage> validationMessages, Object value, Property property) {
		if (value instanceof String) {
			String string = (String) value;
			int maxSize = AnnotationUtil.getSize(property, !AnnotationUtil.OPTIONAL);
			if (string.length() > maxSize) {
				String caption = Resources.getPropertyName(property);
				validationMessages.add(new ValidationMessage(property, MessageFormat.format(Resources.getString("SizeValidator.message"), caption)));
			}
		}
	}

	private static void validateInvalid(List<ValidationMessage> validationMessages, Object value, Property property) {
		if (InvalidValues.isInvalid(value)) {
			validationMessages.add(Validation.createInvalidValidationMessage(property));
		}
	}
	
	public static boolean allUsedFieldsValid(List<ValidationMessage> validationMessages, Collection<Property> properties,
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
