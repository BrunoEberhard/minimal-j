package ch.openech.mj.resources;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.MultiResourceBundle;
import ch.openech.mj.util.StringUtils;

public class Resources {

	private static final ResourceBundle defaultResourcebundle = ResourceBundle.getBundle(Resources.class.getPackage().getName() + ".MinimalJ");
	private static ResourceBundle resourceBundle = defaultResourcebundle;
	
	public static ResourceBundle getDefaultResourcebundle() {
		return defaultResourcebundle;
	}
	
	public static ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	public static void setResourceBundle(ResourceBundle resourceBundle) {
		Resources.resourceBundle = resourceBundle;
	}
	
	public static void addResourceBundle(ResourceBundle resourceBundle) {
		Resources.resourceBundle = new MultiResourceBundle(resourceBundle, defaultResourcebundle);
	}

	public static String getString(String resourceName) {
		String text;
		try {
			text = getResourceBundle().getString(resourceName);
		} catch (MissingResourceException x) {
			System.out.println(resourceName + "=");
			text = "!" + resourceName + "!";
		} catch (NullPointerException x) {
			System.out.println("AbstractApplication.resourceBundle() not set");
			text = resourceName;
		}
		return text;
	}

	public static String getObjectFieldName(ResourceBundle resourceBundle, PropertyInterface property) {
		return getObjectFieldName(resourceBundle, property, null);
	}
	
	public static String getObjectFieldName(ResourceBundle resourceBundle, PropertyInterface property, String postfix) {
		// completeQualifiedKey example: "ch.openech.dm.Person.nationality"
		String fieldName = property.getFieldName();
		if (!StringUtils.isEmpty(postfix)) {
			fieldName += postfix;
		}
		
		String completeQualifiedKey = property.getDeclaringClass().getName() + "." + fieldName;
		if (resourceBundle.containsKey(completeQualifiedKey)) {
			return resourceBundle.getString(completeQualifiedKey);
		}
		
		// qualifiedKey example: "Person.nationality"
		String qualifiedKey = property.getDeclaringClass().getSimpleName() + "." + fieldName;
		if (resourceBundle.containsKey(qualifiedKey)) {
			return resourceBundle.getString(qualifiedKey);
		}

		// unqualifiedKey example: "nationality"
		if (resourceBundle.containsKey(fieldName)) {
			return resourceBundle.getString(fieldName);
		}
		
		// unqualifiedKey example: "Nationality"
		if (resourceBundle.containsKey(StringUtils.upperFirstChar(fieldName))) {
			return resourceBundle.getString(StringUtils.upperFirstChar(fieldName));
		}

		return "!!" + fieldName;
	}
	
}
