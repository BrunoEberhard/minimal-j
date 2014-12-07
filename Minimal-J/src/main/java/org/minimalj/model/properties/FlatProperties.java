package org.minimalj.model.properties;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.util.FieldUtils;
import org.minimalj.util.StringUtils;

public class FlatProperties {
	private static final Logger logger = Logger.getLogger(FlatProperties.class.getName());

	private static final Map<Class<?>, Map<String, PropertyInterface>> properties = 
			new HashMap<Class<?>, Map<String, PropertyInterface>>();

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
			properties.put(clazz, properties(clazz));
		}
		Map<String, PropertyInterface> propertiesForClass = properties.get(clazz);
		return propertiesForClass;
	}
	
	private static Map<String, PropertyInterface> properties(Class<?> clazz) {
		Map<String, PropertyInterface> properties = new HashMap<String, PropertyInterface>();
		
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field)) continue;

			if (!FieldUtils.isFinal(field)) {
				properties.put(field.getName(), new FieldProperty(field));
			} else if (!FieldUtils.isList(field)) {
				Map<String, PropertyInterface> inlinePropertys = properties(field.getType());
				boolean hasClassName = FieldUtils.hasClassName(field);
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = field.getName() + StringUtils.upperFirstChar(inlineKey);
					}
					properties.put(key, new ChainedProperty(clazz, field, inlinePropertys.get(inlineKey)));
				}
			}
		}
		return properties; 
	}
	
	public static List<String> testProperties(Class<?> clazz) {
		List<String> problems = new ArrayList<>();
		Map<String, String> properties = new HashMap<String, String>();
		
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field)) continue;

			if (!FieldUtils.isFinal(field)) {
				String fieldPath = new FieldProperty(field).getFieldPath();
				if (!properties.containsKey(field.getName())) {
					properties.put(field.getName(), fieldPath);
				} else {
					problems.add(field.getName() + " collides with " + properties.get(field.getName()));
				}
			} else if (!FieldUtils.isList(field)) {
				Map<String, PropertyInterface> inlinePropertys = properties(field.getType());
				boolean hasClassName = FieldUtils.hasClassName(field);
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = field.getName() + StringUtils.upperFirstChar(inlineKey);
					}
					if (!properties.containsKey(key)) {
						properties.put(key, new ChainedProperty(clazz, field, inlinePropertys.get(inlineKey)).getFieldPath());
					} else {
						problems.add(key + " collides with " + properties.get(key));
					}
				}
			}
		}
		return problems; 
	}
	
	public static class FieldComparator implements Comparator<Field> {

		@Override
		public int compare(Field o1, Field o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
	
}
