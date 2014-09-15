package org.minimalj.util.resources;

import java.util.ResourceBundle;

import org.minimalj.application.DevMode;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.Code;
import org.minimalj.model.annotation.ViewOf;
import org.minimalj.util.MultiResourceBundle;

public class Resources {

	private static final ResourceBundle defaultResourcebundle = ResourceBundle.getBundle(Resources.class.getPackage().getName() + ".MinimalJ");
	private static ResourceBundle resourceBundle = defaultResourcebundle;
	
	public static final boolean OPTIONAL = false;
	
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

	public static boolean isAvailable(String resourceName) {
		return getResourceBundle().containsKey(resourceName);
	}

	public static String getString(String resourceName) {
		return getString(resourceName, true);
	}
	
	/**
	 * Use the constant OPTIONAL if its not an application error when the resource
	 * is not available.
	 */
	public static String getString(String resourceName, boolean reportIfMissing) {
		if (isAvailable(resourceName)) {
			return getResourceBundle().getString(resourceName);
		} else if (reportIfMissing && DevMode.isActive()) {
			System.out.println(resourceName + "=");
			return "!" + resourceName + "!";
		} else {
			return null;
		}
	}
	
	public static String getString(Class<?> clazz) {
		String result = getStringOrNull(clazz);
		if (result != null) {
			return result;
		} 
		return "!" + getString(clazz.getSimpleName()) + "!";
	}

	private static String getStringOrNull(Class<?> clazz) {
		if (isAvailable(clazz.getName())) {
			return getString(clazz.getName());
		} else if (isAvailable(clazz.getSimpleName())) {
			return getString(clazz.getSimpleName());
		} else if (ViewOf.class.isAssignableFrom(clazz) && !Code.class.isAssignableFrom(clazz)) {
			Class<?> viewedClass = ViewUtil.getViewedClass(clazz);
			String byViewedClass = getStringOrNull(viewedClass);
			if (byViewedClass != null) {
				return byViewedClass;
			}
		}
		return null;
	}

	public static String getObjectFieldName(ResourceBundle resourceBundle, PropertyInterface property) {
		return getObjectFieldName(resourceBundle, property, null);
	}
	
	public static String getObjectFieldName(ResourceBundle resourceBundle, PropertyInterface property, String postfix) {
		String fieldName = property.getFieldName();
		if (postfix != null) {
			fieldName += postfix;
		}
		Class<?> declaringClass = property.getDeclaringClass();
		Class<?> fieldClass = property.getFieldClazz();
		
		return getObjectFieldName(resourceBundle, fieldName, declaringClass, fieldClass);
	}

	private static String getObjectFieldName(ResourceBundle resourceBundle, String fieldName, Class<?> declaringClass, Class<?> fieldClass) {
		// completeQualifiedKey example: "ch.openech.model.Person.nationality"
		String completeQualifiedKey = declaringClass.getName() + "." + fieldName;
		if (resourceBundle.containsKey(completeQualifiedKey)) {
			return resourceBundle.getString(completeQualifiedKey);
		}
		
		// qualifiedKey example: "Person.nationality"
		String qualifiedKey = declaringClass.getSimpleName() + "." + fieldName;
		if (resourceBundle.containsKey(qualifiedKey)) {
			return resourceBundle.getString(qualifiedKey);
		}

		// if declaring class is a view check to viewed class
		if (ViewOf.class.isAssignableFrom(declaringClass) && !Code.class.isAssignableFrom(declaringClass)) {
			Class<?> viewedClass = ViewUtil.getViewedClass(declaringClass);
			return getObjectFieldName(resourceBundle, fieldName, viewedClass, fieldClass);
		}
		
		// class of field
		String byFieldClass = getStringOrNull(fieldClass);
		if (byFieldClass != null) {
			return byFieldClass;
		}
		
		// unqualifiedKey example: "nationality"
		if (resourceBundle.containsKey(fieldName)) {
			return resourceBundle.getString(fieldName);
		}
		
		return "!!" + fieldName;
	}
	
}
