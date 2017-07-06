package org.minimalj.util.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.DevMode;
import org.minimalj.model.Code;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.LocaleContext;

public class Resources {
	private static final Logger logger = Logger.getLogger(Resources.class.getName());

	private static Set<String> resourceBundleNames = new HashSet<>();
	
	public static final boolean OPTIONAL = false;
	
	public static final String APPLICATION_NAME = "Application.name";
	public static final String APPLICATION_ICON = "Application.icon";
	
	private static final Map<Locale, Resources> resourcesByLocale = new HashMap<>();
	
	private ResourceBundle resourceBundle;
	
	private Resources(Locale locale) {
		resourceBundle = ResourceBundle.getBundle(Resources.class.getPackage().getName() + ".MinimalJ", locale, Control.getNoFallbackControl(Control.FORMAT_PROPERTIES));
		for (String resourceBundleName : resourceBundleNames) {
			resourceBundle = new MultiResourceBundle(resourceBundle, ResourceBundle.getBundle(resourceBundleName, locale, Control.getNoFallbackControl(Control.FORMAT_PROPERTIES)));
		}
	}
	
	public static ResourceBundle getResourceBundle() {
		Locale locale = LocaleContext.getCurrent();
		if (!resourcesByLocale.containsKey(locale)) {
			resourcesByLocale.put(locale, new Resources(locale));
		}
		return resourcesByLocale.get(locale).resourceBundle;
	}

	public static void addResourceBundleName(String resourceBundleName) {
		resourceBundleNames.add(resourceBundleName);
		// normally resource bundles are only added at startup. But for tests the
		// cache has to be cleared.
		resourcesByLocale.clear();
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

	public static String getString(String resourceName) {
		return getString(resourceName, true);
	}
	
	/**
	 * @param resourceName the name of the resource. No further prefixes or postfixes are applied
	 * @param reportIfMissing Use the constant OPTIONAL if its not an application error when the resource is not available
	 * @return the String or 'resourceName' if the resourceName does not exist
	 */
	public static String getString(String resourceName, boolean reportIfMissing) {
		if (isAvailable(resourceName)) {
			return getResourceBundle().getString(resourceName);
		} else {
			reportMissing(resourceName, reportIfMissing);
			return "'" + resourceName + "'";
		}
	}
	
	private static final Set<String> missing = new TreeSet<>();
	
	public static void reportMissing(String resourceName, boolean reportIfMissing) {
		if (reportIfMissing && DevMode.isActive()) {
			missing.add(resourceName);
		}
	}

	public static void printMissing() {
		missing.stream().forEach(s -> System.out.println(s + " = " ));
	}
	
	public static String getString(Class<?> clazz) {
		String result = getStringOrNull(clazz);
		if (result != null) {
			return result;
		} 
		return getString(clazz.getSimpleName());
	}

	public static String getStringOrNull(Class<?> clazz) {
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
	
	public static String getPropertyName(PropertyInterface property) {
		return getPropertyName(getResourceBundle(), property);
	}

	public static String getPropertyName(PropertyInterface property, String postfix) {
		String result = getPropertyName(getResourceBundle(), property, postfix);
		if (result == null) {
			// if no resource with postfix try without (need for checkboxes)
			result = getPropertyName(getResourceBundle(), property);
		}
		return result;
	}
	
	private static String getPropertyName(ResourceBundle resourceBundle, PropertyInterface property) {
		return getPropertyName(resourceBundle, property, null);
	}
	
	private static String getPropertyName(ResourceBundle resourceBundle, PropertyInterface property, String postfix) {
		String fieldName = property.getName();
		if (postfix != null) {
			fieldName += postfix;
		}
		Class<?> declaringClass = property.getDeclaringClass();
		Class<?> fieldClass = property.getClazz();
		
		return getPropertyName(resourceBundle, fieldName, declaringClass, fieldClass, postfix != null);
	}

	private static String getPropertyName(ResourceBundle resourceBundle, String fieldName, Class<?> declaringClass, Class<?> fieldClass, boolean optional) {
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
			return getPropertyName(resourceBundle, fieldName, viewedClass, fieldClass, optional);
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
		
		if (!optional) {
			reportMissing(qualifiedKey, true);
			return "'" + qualifiedKey + "'";
		} else {
			return null;
		}
	}
	
	public static String getResourceName(Class<?> clazz) {
		if (clazz.isAnonymousClass()) {
			clazz = clazz.getSuperclass();
		}
		
		Class<?> c = clazz;
		while (c != Object.class) {
			if (Resources.isAvailable(c.getName())) {
				return c.getName();
			} 
			
			if (Resources.isAvailable(c.getSimpleName())) {
				return c.getSimpleName();
			}
			c = c.getSuperclass();
		}
		
		return clazz.getSimpleName();
	}

	private static Map<String, String> mimeTypeByPostfix = new HashMap<>();
	
	static {
		mimeTypeByPostfix.put("html", "text/html");
		mimeTypeByPostfix.put("css", "text/css");
		mimeTypeByPostfix.put("js", "application/javascript");
		mimeTypeByPostfix.put("jpg", "image/jpg");
		mimeTypeByPostfix.put("png", "image/png");
	}
	
	public static void addMimeType(String postfix, String contentType) {
		mimeTypeByPostfix.put(postfix, contentType);
	}
	
	public static String getMimeType(String postfix) {
		return mimeTypeByPostfix.get(postfix);
	}

	// combination of NoFallbackControl and EncodingResourceBundleControl (all final)
	private static class NoFallbackUTF8Control extends Control {

		@Override
		public List<String> getFormats(String baseName) {
			Objects.requireNonNull(baseName);
			return Control.FORMAT_PROPERTIES;
		}

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale) {
			Objects.requireNonNull(baseName);
			Objects.requireNonNull(locale);
			return null;
		}

		@Override
		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, "properties");
			URL resourceURL = loader.getResource(resourceName);
			if (resourceURL != null) {
				try {
					return new PropertyResourceBundle(new InputStreamReader(resourceURL.openStream(), Charset.forName("UTF-8")));
				} catch (Exception z) {
					logger.log(Level.FINE, "exception thrown during bundle initialization", z);
				}
			}
			return super.newBundle(baseName, locale, format, loader, reload);
		}
	}
}
