package ch.openech.mj.model;

import java.util.List;
import java.util.ResourceBundle;

import ch.openech.mj.db.EmptyObjects;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.edit.value.Properties;
import ch.openech.mj.edit.value.Required;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.util.StringUtils;

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
		validate(resultList, object, Constants.getProperty(key));
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
