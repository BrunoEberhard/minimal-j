package org.minimalj.util.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.page.Page;
import org.minimalj.model.Code;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtils;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.Property;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.StringUtils;

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
			ResourceBundle frameworkResourceBundle = ResourceBundle.getBundle("MinimalJ", locale, Control.getNoFallbackControl(Control.FORMAT_PROPERTIES));
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

	public static String getPropertyName(Property property) {
		return getAccess().getPropertyName(property, null);
	}

	public static String getPropertyName(Property property, String postfix) {
		return getAccess().getPropertyName(property, "." + postfix);
	}

	public static String getResourceName(Class<?> clazz) {
		return getAccess().getResourceName(clazz);
	}

	public static String getPageTitle(Page page) {
		return getString(page.getClass());
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
				String integerString = doGetString(resourceName);
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
				return doGetString(resourceName);
			} else {
				reportMissing(resourceName, reportIfMissing);
				return "'" + resourceName + "'";
			}
		}

		private String doGetString(String resourceName) {
			logger.finest(resourceName);
			return fillPlaceHolder(resourceBundle.getString(resourceName));
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
			} else if (isAvailable(StringUtils.lowerFirstChar(clazz.getSimpleName()))) {
				return getString(StringUtils.lowerFirstChar(clazz.getSimpleName()));				
			} else if (View.class.isAssignableFrom(clazz) && !Code.class.isAssignableFrom(clazz)) {
				Class<?> viewedClass = ViewUtils.getViewedClass(clazz);
				String byViewedClass = getStringOrNull(viewedClass);
				if (byViewedClass != null) {
					return byViewedClass;
				}
			}
			return null;
		}

		String getPropertyName(Property property, String postfix) {
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
			List<Property> chain = chainedProperty.getChain();
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
				return doGetString(completeQualifiedKey);
			}

			// qualifiedKey example: "Person.nationality"
			String qualifiedKey = declaringClass.getSimpleName() + "." + fieldName;
			if (resourceBundle.containsKey(qualifiedKey)) {
				return doGetString(qualifiedKey);
			}

			// if declaring class is a view check to viewed class
			if (View.class.isAssignableFrom(declaringClass) && !Code.class.isAssignableFrom(declaringClass)) {
				Class<?> viewedClass = ViewUtils.getViewedClass(declaringClass);
				return getPropertyName(fieldName, viewedClass, fieldClass, optional);
			}

			// class of field
			String byFieldClass = getStringOrNull(fieldClass);
			if (byFieldClass != null) {
				return byFieldClass;
			}

			// unqualifiedKey example: "nationality"
			if (resourceBundle.containsKey(fieldName)) {
				return doGetString(fieldName);
			}
			
			// class of same name
			String className = getStringOrNull(fieldClass);
			if (className != null) {
				return className;
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

				String simpleName = c.getSimpleName();
				if (isAvailable(simpleName)) {
					return simpleName;
				}
				
				simpleName = StringUtils.lowerFirstChar(c.getSimpleName());
				if (isAvailable(simpleName)) {
					return StringUtils.lowerFirstChar(simpleName);
				}
				
				c = c.getSuperclass();
			}

			return clazz.getSimpleName();
		}
		
		private Pattern pattern = Pattern.compile("\\$\\{([\\w\\.]+)\\}");

		private String fillPlaceHolder(String text) {
			if (text.contains("${")) {
				StringBuffer sb = new StringBuffer();
				Matcher matcher = pattern.matcher(text);
				while (matcher.find()) {
					String replacement = getString(matcher.group(1));
					matcher.appendReplacement(sb, replacement);
				}
				matcher.appendTail(sb);
				return sb.toString();
			} else {
				return text;
			}
		}
	}

	//

	private static final Set<String> missing = new TreeSet<>();

	private static void reportMissing(String resourceName, boolean reportIfMissing) {
		if (reportIfMissing && Configuration.get("MjPrintMissingResources", "false").equals("true")) {
			missing.add(resourceName);
		}
	}

	public static void printMissing() {
		missing.stream().forEach(s -> System.out.println(s + " = "));
	}

	//

	private static Map<String, String> mimeTypeByPostfix = new HashMap<>();

	static {
		mimeTypeByPostfix.put("html", "text/html;charset=UTF-8");
		mimeTypeByPostfix.put("css", "text/css;charset=UTF-8");
		mimeTypeByPostfix.put("js", "application/javascript;charset=UTF-8");
		mimeTypeByPostfix.put("jpg", "image/jpg");
		mimeTypeByPostfix.put("png", "image/png");
		mimeTypeByPostfix.put("webp", "image/webp");
		mimeTypeByPostfix.put("woff2", "font/woff2");
	}

	public static void addMimeType(String postfix, String contentType) {
		mimeTypeByPostfix.put(postfix, contentType);
	}

	public static String getMimeType(String postfix) {
		return mimeTypeByPostfix.get(postfix);
	}
}
