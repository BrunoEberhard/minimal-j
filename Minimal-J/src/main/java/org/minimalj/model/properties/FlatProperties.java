package org.minimalj.model.properties;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.model.PropertyInterface;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.StringUtils;

public class FlatProperties {
	private static final Logger logger = Logger.getLogger(FlatProperties.class.getName());

	private static final Map<Class<?>, Map<String, PropertyInterface>> properties = 
			new HashMap<Class<?>, Map<String, PropertyInterface>>();

	private static final Map<Class<?>, Map<String, PropertyInterface>> propertiesWithLists = 
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
			properties.put(clazz, properties(clazz, false));
		}
		Map<String, PropertyInterface> propertiesForClass = properties.get(clazz);
		return propertiesForClass;
	}
	
	public static Map<String, PropertyInterface> getPropertiesWithLists(Class<?> clazz) {
		if (!propertiesWithLists.containsKey(clazz)) {
			propertiesWithLists.put(clazz, properties(clazz, true));
		}
		Map<String, PropertyInterface> propertiesForClass = propertiesWithLists.get(clazz);
		return propertiesForClass;
	}
	
	private static Map<String, PropertyInterface> properties(Class<?> clazz, boolean withLists) {
		Map<String, PropertyInterface> properties = new LinkedHashMap<String, PropertyInterface>();
		
		Field[] fields = clazz.getFields();
		// there is no contrat in the jvm that fields have to keep the declared
		// order. As these properties are used for hash they have to be always in
		// the same order. Thats done with sorting them alphabetically.
		Arrays.sort(fields, new FieldComparator());
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field)) continue;

			if (!FieldUtils.isFinal(field) || FieldUtils.isList(field) && withLists) {
				properties.put(field.getName(), new FieldProperty(clazz, field));
			} else if (!FieldUtils.isList(field)) {
				Map<String, PropertyInterface> inlinePropertys = properties(field.getType(), withLists);
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
		Map<String, String> properties = new LinkedHashMap<String, String>();
		
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field)) continue;

			if (!FieldUtils.isFinal(field)) {
				String fieldPath = new FieldProperty(clazz, field).getFieldPath();
				if (!properties.containsKey(field.getName())) {
					properties.put(field.getName(), fieldPath);
				} else {
					problems.add(field.getName() + " collides with " + properties.get(field.getName()));
				}
			} else if (!FieldUtils.isList(field)) {
				Map<String, PropertyInterface> inlinePropertys = properties(field.getType(), false);
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
