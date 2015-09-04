package org.minimalj.util.resources;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.minimalj.application.DevMode;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.model.Code;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.MultiResourceBundle;

public class Resources {
	private static final Logger logger = Logger.getLogger(Resources.class.getName());
	private static final String ICONS_DIRECTORY = "icons";

	private static Set<String> resourceBundleNames = new HashSet<>();
	
	public static final boolean OPTIONAL = false;
	
	public static final String APPLICATION_TITLE = "Application.title";
	public static final String APPLICATION_VENDOR = "Application.vendor";
	public static final String APPLICATION_HOMEPAGE = "Application.homepage";
	public static final String APPLICATION_VERSION = "Application.version";

	private static final Map<Locale, Resources> resourcesByLocale = new HashMap<>();
	
	private ResourceBundle resourceBundle;
	
	private Resources(Locale locale) {
		resourceBundle = ResourceBundle.getBundle(Resources.class.getPackage().getName() + ".MinimalJ", locale);
		for (String resourceBundleName : resourceBundleNames) {
			resourceBundle = new MultiResourceBundle(resourceBundle, ResourceBundle.getBundle(resourceBundleName, locale));
		}
	}
	
	public static ResourceBundle getResourceBundle() {
		Locale locale = LocaleContext.getLocale();
		if (!resourcesByLocale.containsKey(locale)) {
			resourcesByLocale.put(locale, new Resources(locale));
		}
		return resourcesByLocale.get(locale).resourceBundle;
	}

	public static void addResourceBundleName(String resourceBundleName) {
		resourceBundleNames.add(resourceBundleName);
	}

	public static boolean isAvailable(String resourceName) {
		return getResourceBundle().containsKey(resourceName);
	}
	
	public static Integer getInteger(String resourceName, boolean reportIfMissing) {
		if (Resources.isAvailable(resourceName)) {
			String integerString = Resources.getString(resourceName);
			try {
				return Integer.parseInt(integerString);
			} catch (NumberFormatException nfe) {
				logger.warning("Number format wrong for resource " + resourceName + "('" + integerString + "')");
				return null;
			}
		} else {
			reportMissing(resourceName, reportIfMissing);
			return null;
		}
	}
	
	public static Icon getIconByResourceName(String resourceName) {
		if (Resources.isAvailable(resourceName)) {
			String filename = Resources.getString(resourceName);
			URL url = Resources.class.getResource(ICONS_DIRECTORY + "/" + filename);
			if (url != null) {
				return new ImageIcon(url);
			}
		}
		return null;
	}
	
	public static Icon getIcon(String filename) {
		filename = ICONS_DIRECTORY + "/" + filename;
		URL url = Resources.class.getResource(filename);
		if (url != null) {
			return new ImageIcon(url);
		} else {
			return null;
		}
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
		} else {
			reportMissing(resourceName, reportIfMissing);
			return "'" + resourceName + "'";
		}
	}
	
	private static final Set<String> reported = new TreeSet<>();
	
	public static void reportMissing(String resourceName, boolean reportIfMissing) {
		if (reportIfMissing && DevMode.isActive() && !reported.contains(resourceName)) {
			reported.add(resourceName);
			System.out.println(resourceName + "=");
		}
	}
	
	public static String getString(Class<?> clazz) {
		String result = getStringOrNull(clazz);
		if (result != null) {
			return result;
		} 
		return getString(clazz.getSimpleName());
	}

	private static String getStringOrNull(Class<?> clazz) {
		if (isAvailable(clazz.getName())) {
			return getString(clazz.getName());
		} else if (isAvailable(clazz.getSimpleName())) {
			return getString(clazz.getSimpleName());
		} else if (View.class.isAssignableFrom(clazz) && !Code.class.isAssignableFrom(clazz)) {
			Class<?> viewedClass = ViewUtil.getViewedClass(clazz);
			String byViewedClass = getStringOrNull(viewedClass);
			if (byViewedClass != null) {
				return byViewedClass;
			}
		}
		return null;
	}

	//
	
	public static String getObjectFieldName(PropertyInterface property) {
		return getObjectFieldName(getResourceBundle(), property);
	}

	public static String getObjectFieldName(PropertyInterface property, String postfix) {
		return getObjectFieldName(getResourceBundle(), property, postfix);
	}
	
	public static String getObjectFieldName(ResourceBundle resourceBundle, PropertyInterface property) {
		return getObjectFieldName(resourceBundle, property, null);
	}
	
	public static String getObjectFieldName(ResourceBundle resourceBundle, PropertyInterface property, String postfix) {
		String fieldName = property.getName();
		if (postfix != null) {
			fieldName += postfix;
		}
		Class<?> declaringClass = property.getDeclaringClass();
		Class<?> fieldClass = property.getClazz();
		
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
		if (View.class.isAssignableFrom(declaringClass) && !Code.class.isAssignableFrom(declaringClass)) {
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
		
		return "'" + qualifiedKey + "'";
	}
	
	public static String getActionResourceName(Class<?> clazz) {
		// qualified name of action
		if (Resources.isAvailable(clazz.getName())) {
			return clazz.getName();
		} 

		// qualified name of edited class
		Class<?> clazz2 = null;
		if (Editor.class.isAssignableFrom(clazz)) {
			clazz2 = GenericUtils.getGenericClass(clazz);
		}
		if (clazz2 != null && Resources.isAvailable(clazz2.getName())) {
			return clazz2.getName();
		}
		
		// simple name of action
		if (Resources.isAvailable(clazz.getSimpleName())) {
			return clazz.getSimpleName();
		}
		
		// simple name of edited class
		if (clazz2 != null && Resources.isAvailable(clazz2.getSimpleName())) {
			return clazz2.getSimpleName();
		}
		
		return clazz.getSimpleName();
	}
	
}
