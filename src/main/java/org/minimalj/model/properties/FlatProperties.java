package org.minimalj.model.properties;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.util.FieldUtils;
import org.minimalj.util.StringUtils;

public class FlatProperties {
	private static final Logger logger = Logger.getLogger(FlatProperties.class.getName());

	private static final Map<Class<?>, Map<String, PropertyInterface>> properties =
			new HashMap<>();

	public static PropertyInterface getProperty(Class<?> clazz, String fieldName) {
		return getProperty(clazz, fieldName, false);
	}
	
	public static PropertyInterface getProperty(Class<?> clazz, String fieldName, boolean safe) {
		if (fieldName == null) throw new NullPointerException();

		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		PropertyInterface propertyInterface = propertiesForClass.get(fieldName);

		if (propertyInterface == null && !safe) throw new IllegalArgumentException("No field/setMethod " + fieldName + " in Class " + clazz.getName());

		return propertyInterface;
	}

	public static boolean hasProperty(Class<?> clazz, String fieldName) {
		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		return propertiesForClass.containsKey(fieldName);
	}

	public static PropertyInterface getProperty(Field field) {
		return getProperty(field.getDeclaringClass(), field.getName());
	}
	
	public static Object getValue(Object domainObject, String key) {
		Class<?> clazz = domainObject.getClass();
		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		PropertyInterface propertyInterface = propertiesForClass.get(key);
		if (propertyInterface != null) {
			return propertyInterface.getValue(domainObject);
		} else {
			logger.severe("No column " + key + " in Class " + clazz.getName());
			return null;
		}
	}
	
	public static void set(Object object, String fieldName, Object value) {
		if (fieldName == null) throw new NullPointerException();
		if (object == null) throw new NullPointerException();
		
		getProperty(object.getClass(), fieldName).setValue(object, value);
	}
	
	public static Map<String, PropertyInterface> getProperties(Class<?> clazz) {
		if (!properties.containsKey(clazz)) {
			properties.put(clazz, Collections.unmodifiableMap(properties(clazz)));
		}
		Map<String, PropertyInterface> propertiesForClass = properties.get(clazz);
		return propertiesForClass;
	}
	
	private static Map<String, PropertyInterface> properties(Class<?> clazz) {
		// Java doesn't guarantee the field / property order but most of the time the
		// order is as in the class described. Keep it that way for json/xml/yaml... serialization stuff.
		Map<String, PropertyInterface> properties = new LinkedHashMap<>();
		
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field)) continue;

			if (!FieldUtils.isFinal(field) || FieldUtils.isSet(field.getType()) || FieldUtils.isList(field.getType())) {
				properties.put(field.getName(), new FieldProperty(field));
			} else {
				Map<String, PropertyInterface> inlinePropertys = properties(field.getType());
				boolean hasClassName = FieldUtils.hasClassName(field) && !hasCollidingFields(clazz, field.getType(), field.getName());
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = field.getName() + StringUtils.upperFirstChar(inlineKey);
					}
					properties.put(key, new ChainedProperty(new FieldProperty(field), inlinePropertys.get(inlineKey)));
				}
			}
		}
		return properties; 
	}
	
	public static List<PropertyInterface> getListProperties(Class<?> clazz) {
		List<PropertyInterface> properties = new ArrayList<>();
		
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field)) continue;

			if (FieldUtils.isList(field)) {
				properties.add(new FieldProperty(field));
			} else if (FieldUtils.isFinal(field)) {
				List<PropertyInterface> inlineProperties = getListProperties(field.getType());
				for (PropertyInterface inlineProperty : inlineProperties) {
					properties.add(new ChainedProperty(new FieldProperty(field), inlineProperty));
				}
			}
		}
		return properties; 
	}
	
	public static boolean hasCollidingFields(Class<?> clazz, Class<?> clazz2, String ignore) {
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field) || field.getName().equals(ignore)) continue;
			Field[] fields2 = clazz2.getFields();
			for (Field field2 : fields2) {
				if (FieldUtils.isTransient(field2) || FieldUtils.isStatic(field2)) continue;
				if (field.getName().equals(field2.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public static class FieldComparator implements Comparator<Field> {

		@Override
		public int compare(Field o1, Field o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
	
}
