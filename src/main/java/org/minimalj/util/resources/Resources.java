package org.minimalj.util.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
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

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.model.Code;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.LocaleContext;

public class Resources {
	private static final Logger logger = Logger.getLogger(Resources.class.getName());

	public static final boolean OPTIONAL = false;
	
	public static final String APPLICATION_NAME = "Application.name";
	public static final String APPLICATION_ICON = "Application.icon";
	
	private static final Map<Locale, ResourceBundleAccess> resourcesByLocale = new HashMap<>();
	
	private static ResourceBundleAccess getAccess() {
		Locale locale = LocaleContext.getCurrent();
		if (!resourcesByLocale.containsKey(locale)) {
			ResourceBundle resourceBundle = Application.getInstance().getResourceBundle(locale);
			ResourceBundle frameworkResourceBundle = ResourceBundle.getBundle(Resources.class.getPackage().getName() + ".MinimalJ", locale, Control.getNoFallbackControl(Control.FORMAT_PROPERTIES));
			resourcesByLocale.put(locale, new ResourceBundleAccess(new MultiResourceBundle(resourceBundle, frameworkResourceBundle)));
		}
		return resourcesByLocale.get(locale);
	}

	public static boolean isAvailable(String resourceName) {
		return getAccess().isAvailable(resourceName);
	}
	
	public static Integer getInteger(String resourceName, boolean reportIfMissing) {
		return getAccess().getInteger(resourceName, reportIfMissing);
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
		return getAccess().getString(resourceName, reportIfMissing);
	}
	
	public static String getString(Class<?> clazz) {
		return getAccess().getString(clazz);
	}

	public static String getStringOrNull(Class<?> clazz) {
		return getAccess().getStringOrNull(clazz);
	}

	public static String getPropertyName(PropertyInterface property) {
		return getAccess().getPropertyName(property, null);
	}

	public static String getPropertyName(PropertyInterface property, String postfix) {
		String result = getAccess().getPropertyName(property, postfix);
		if (result == null) {
			// if no resource with postfix try without (need for checkboxes)
			result = getAccess().getPropertyName(property, null);
		}
		return result;
	}
	
	public static String getResourceName(Class<?> clazz) {
		return getAccess().getResourceName(clazz);
	}

	//
	
	/* test */ static class ResourceBundleAccess {
		private final ResourceBundle resourceBundle;

		ResourceBundleAccess(ResourceBundle resourceBundle) {
			this.resourceBundle = resourceBundle;
		}

		boolean isAvailable(String resourceName) {
			return resourceBundle.containsKey(resourceName);
		}

		Integer getInteger(String resourceName, boolean reportIfMissing) {
			if (isAvailable(resourceName)) {
				String integerString = getString(resourceName);
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

		String getString(String resourceName) {
			return getString(resourceName, true);
		}

		String getString(String resourceName, boolean reportIfMissing) {
			if (isAvailable(resourceName)) {
				return resourceBundle.getString(resourceName);
			} else {
				reportMissing(resourceName, reportIfMissing);
				return "'" + resourceName + "'";
			}
		}

		String getString(Class<?> clazz) {
			String result = getStringOrNull(clazz);
			if (result != null) {
				return result;
			}
			return getString(clazz.getSimpleName());
		}

		String getStringOrNull(Class<?> clazz) {
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

		String getPropertyName(PropertyInterface property, String postfix) {
			if (property instanceof ChainedProperty) {
				ChainedProperty chainedProperty = (ChainedProperty) property;
				return getProperty(chainedProperty, postfix);
			}

			Class<?> fieldClass = property.getClazz();
			String fieldName = property.getName();
			if (postfix != null)
				fieldName += postfix;
			Class<?> declaringClass = property.getDeclaringClass();

			return getPropertyName(fieldName, declaringClass, fieldClass, postfix != null);
		}

		private String getProperty(ChainedProperty chainedProperty, String postfix) {
			List<PropertyInterface> chain = chainedProperty.getChain();
			String fieldName = chainedProperty.getPath();
			if (postfix != null)
				fieldName += postfix;
			while (chain.size() > 1) {
				String result = getPropertyName(fieldName, chain.get(0).getDeclaringClass(), chainedProperty.getClazz(), true);
				if (result != null) {
					return result;
				} else {
					chain = chain.subList(1, chain.size());
					fieldName = fieldName.substring(fieldName.indexOf('.') + 1);
				}
			}
			return getPropertyName(chain.get(0), postfix);
		}

		String getPropertyName(String fieldName, Class<?> declaringClass, Class<?> fieldClass, boolean optional) {
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
				return getPropertyName(fieldName, viewedClass, fieldClass, optional);
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

		String getResourceName(Class<?> clazz) {
			if (clazz.isAnonymousClass()) {
				clazz = clazz.getSuperclass();
			}

			Class<?> c = clazz;
			while (c != Object.class) {
				if (isAvailable(c.getName())) {
					return c.getName();
				}

				if (isAvailable(c.getSimpleName())) {
					return c.getSimpleName();
				}
				c = c.getSuperclass();
			}

			return clazz.getSimpleName();
		}
	}
	
	//
	
	private static final Set<String> missing = new TreeSet<>();

	private static void reportMissing(String resourceName, boolean reportIfMissing) {
		if (reportIfMissing && Configuration.isDevModeActive()) {
			missing.add(resourceName);
		}
	}

	public static void printMissing() {
		missing.stream().forEach(s -> System.out.println(s + " = "));
	}
	
	//
	
	private static Map<String, String> mimeTypeByPostfix = new HashMap<>();
	
	static {
		mimeTypeByPostfix.put("html", "text/html");
		mimeTypeByPostfix.put("css", "text/css");
		mimeTypeByPostfix.put("js", "application/javascript");
		mimeTypeByPostfix.put("jpg", "image/jpg");
		mimeTypeByPostfix.put("png", "image/png");
		// TODO restrict to cheerpj?
		mimeTypeByPostfix.put("jar", "application/java");
	}
	
	public static void addMimeType(String postfix, String contentType) {
		mimeTypeByPostfix.put(postfix, contentType);
	}
	
	public static String getMimeType(String postfix) {
		return mimeTypeByPostfix.get(postfix);
	}

	// combination of NoFallbackControl and EncodingResourceBundleControl (all final)
	// in Java 9 there are UTF-8 property - files :)
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
