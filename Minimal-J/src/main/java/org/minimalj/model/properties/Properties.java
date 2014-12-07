package org.minimalj.model.properties;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.util.FieldUtils;

public class Properties {
	private static final Logger logger = Logger.getLogger(Properties.class.getName());

	private static final Map<Class<?>, Map<String, PropertyInterface>> properties = 
			new HashMap<Class<?>, Map<String, PropertyInterface>>();
	
	public static PropertyInterface getProperty(Class<?> clazz, String fieldName) {
		if (fieldName == null) throw new NullPointerException();

		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		PropertyInterface propertyInterface = propertiesForClass.get(fieldName);

		if (propertyInterface != null) {
			return propertyInterface;
		} else {
			logger.severe("No field/setMethod " + fieldName + " in Class " + clazz.getName());
			return null;
		}
	}

	public static PropertyInterface getProperty(Field field) {
		return getProperty(field.getDeclaringClass(), field.getName());
	}
	
	public static void set(Object object, String fieldName, Object value) {
		if (fieldName == null) throw new NullPointerException();
		if (object == null) throw new NullPointerException();
		
		getProperty(object.getClass(), fieldName).setValue(object, value);
	}
	
	public static Map<String, PropertyInterface> getProperties(Class<?> clazz) {
		if (!properties.containsKey(clazz)) {
			properties.put(clazz, properties(clazz));
		}
		Map<String, PropertyInterface> propertiesForClass = properties.get(clazz);
		return propertiesForClass;
	}
	
	private static Map<String, PropertyInterface> properties(Class<?> clazz) {
		Map<String, PropertyInterface> properties = new LinkedHashMap<String, PropertyInterface>();
		
		for (Field field : clazz.getFields()) {
			if (FieldUtils.isTransient(field)) continue;

			if (!FieldUtils.isStatic(field)) {
				properties.put(field.getName(), new FieldProperty(field));
			} 
		}
		return properties; 
	}
	
}
