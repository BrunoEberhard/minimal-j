package org.minimalj.model.properties;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import org.minimalj.model.Keys;
import org.minimalj.util.FieldUtils;

public class Properties {
	private static final Logger logger = Logger.getLogger(Properties.class.getName());

	private static final Map<Class<?>, Map<String, PropertyInterface>> properties = new HashMap<>();
	private static final Map<Class<?>, Map<String, PropertyInterface>> methodProperties = new HashMap<>();

	public static PropertyInterface getProperty(Class<?> clazz, String propertyName) {
		Objects.requireNonNull(propertyName);

		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		PropertyInterface property = propertiesForClass.get(propertyName);

		if (property == null) {
			property = getMethodProperty(clazz, propertyName);
		}

		if (property != null) {
			return property;
		} else {
			logger.fine("No field/access methods for " + propertyName + " in Class " + clazz.getName());
			return null;
		}
	}

	public static PropertyInterface getMethodProperty(Class<?> clazz, String propertyName) {
		Map<String, PropertyInterface> methodPropertiesForClass = methodProperties.computeIfAbsent(clazz, c -> new HashMap<>());

		PropertyInterface property = methodPropertiesForClass.computeIfAbsent(propertyName, name -> Keys.getMethodProperty(clazz, name));
		return property;
	}

	public static PropertyInterface getPropertyByPath(Class<?> clazz, String propertyName) {
		int pos = propertyName.indexOf('.');
		if (pos < 0) {
			return getProperty(clazz, propertyName);
		} else {
			PropertyInterface property1 = getProperty(clazz, propertyName.substring(0, pos));
			PropertyInterface property2 = getPropertyByPath(property1.getClazz(), propertyName.substring(pos + 1));
			return new ChainedProperty(property1, property2);
		}
	}

	public static PropertyInterface getProperty(Field field) {
		return getProperty(field.getDeclaringClass(), field.getName());
	}

	public static Map<String, PropertyInterface> getProperties(Class<?> clazz) {
		if (!properties.containsKey(clazz)) {
			properties.put(clazz, Collections.unmodifiableMap(properties(clazz)));
		}
		Map<String, PropertyInterface> propertiesForClass = properties.get(clazz);
		return propertiesForClass;
	}

	private static Map<String, PropertyInterface> properties(Class<?> clazz) {
		Map<String, PropertyInterface> properties = new LinkedHashMap<>();

		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isStatic(field)) {
				properties.put(field.getName(), new FieldProperty(field, clazz));
			}
		}
		return properties;
	}

}
