package org.minimalj.model.validation;

import java.util.List;
import java.util.ResourceBundle;

import org.minimalj.backend.db.EmptyObjects;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Required;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class EmptyValidator {

	public static void validate(Object object, List<ValidationMessage> resultList) {
		validate(object, resultList, Resources.getResourceBundle());
	}
	
	public static void validate(Object object, List<ValidationMessage> resultList, ResourceBundle resourceBundle) {
		for (PropertyInterface property : Properties.getProperties(object.getClass()).values()) {
			boolean required = property.getAnnotation(Required.class) != null;
			if (required) {
				validate(resultList, object, property, resourceBundle);
			}
		}
		
	}

	public static void validate(List<ValidationMessage> resultList, Object object, Object key) {
		validate(resultList, object, Keys.getProperty(key));
	}
	
	public static void validate(List<ValidationMessage> resultList, Object object, PropertyInterface property) {
		validate(resultList, object, property, Resources.getResourceBundle());
	}
	
	public static void validate(List<ValidationMessage> resultList, Object object, PropertyInterface property, ResourceBundle resourceBundle) {
		Object value = property.getValue(object);
		if (EmptyObjects.isEmpty(value)) {
			String caption = Resources.getObjectFieldName(resourceBundle, property);
			if (StringUtils.isEmpty(caption)) {
				caption = "Eingabe";
			}
			resultList.add(new ValidationMessage(property, caption + " erforderlich"));
		}
	}
}
