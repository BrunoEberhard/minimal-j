package ch.openech.mj.db.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.annotation.Reference;
import ch.openech.mj.model.annotation.Required;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.StringUtils;

/**
 * Framework internal<p>
 * 
 * Provides for each class a map of Properties. But only the
 * properties that are <b>not</b> of the class List. These Properties
 * are the columns of the database tables.
 *
 */
public class ColumnProperties {
	private static final Logger logger = Logger.getLogger(ColumnProperties.class.getName());
	
	private static final Map<Class<?>, Map<String, PropertyInterface>> properties = 
		new HashMap<Class<?>, Map<String, PropertyInterface>>();

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
	
	public static Class<?> getType(Class<?> clazz, String key) {
		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		PropertyInterface propertyInterface = propertiesForClass.get(key);
		if (propertyInterface != null) {
			return propertyInterface.getFieldClazz();
		} else {
			logger.severe("No column " + key + " in Class " + clazz.getName());
			return null;
		}
	}

	public static Object getValueIgnoreCase(Object domainObject, String key) {
		Class<?> clazz = domainObject.getClass();
		PropertyInterface propertyInterface = getPropertyIgnoreCase(clazz, key);
		if (propertyInterface != null) {
			return propertyInterface.getValue(domainObject);
		} else {
			logger.severe("No column " + key + " in Class " + clazz.getName());
			return null;
		}
	}
	
	public static void setValue(Object domainObject, String key, Object value) {
		Class<?> clazz = domainObject.getClass();
		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		PropertyInterface property = propertiesForClass.get(key);
		if (property != null) {
			property.setValue(domainObject, value);
		} else {
			throw new IllegalArgumentException("No column " + key +" on " + domainObject.getClass().getName());
		}
	}
	
	public static void setValueIgnoreCase(Object domainObject, String key, Object value) {
		Class<?> clazz = domainObject.getClass();
		PropertyInterface propertyInterface = getPropertyIgnoreCase(clazz, key);
		if (propertyInterface != null) {
			propertyInterface.setValue(domainObject, value);
		}
	}

	public static PropertyInterface getPropertyIgnoreCase(Class<?> clazz, String key) {
		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		PropertyInterface propertyInterface = null;
		for (Map.Entry<String, PropertyInterface> entry : propertiesForClass.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(key)) {
				propertyInterface = entry.getValue();
			}
		}
		return propertyInterface;
	}

	public static Set<String> getKeys(Class<?> clazz) {
		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		return propertiesForClass.keySet();
	}
	
	public static List<String> getNonListKeys(Class<?> clazz) {
		Map<String, PropertyInterface> propertiesForClass = getProperties(clazz);
		List<String> keys = new ArrayList<String>();
		for (Map.Entry<String, PropertyInterface> entry : propertiesForClass.entrySet()) {
			if (!FieldUtils.isList(entry.getValue().getFieldClazz())) {
				keys.add(entry.getKey());
			}
		}
		return keys;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void copy(Object from, Object to) {
		Map<String, PropertyInterface> properties = getProperties(from.getClass());
		for (PropertyInterface property : properties.values()) {
			Object fromValue = property.getValue(from);
			if (property.isFinal()) {
				Object toValue = property.getValue(to);
				copy(fromValue, toValue);
			} else {
				property.setValue(to, fromValue);
			}
		}
		properties = ListColumnProperties.getProperties(from.getClass());
		for (PropertyInterface property : properties.values()) {
			List fromList = (List) property.getValue(from);
			List toList = (List)property.getValue(to);
			if (fromList != toList) {
				toList.clear();
				toList.addAll(fromList);
			}
		}
	}
	
	//

	public static Map<String, PropertyInterface> getProperties(Class<?> clazz) {
		if (!properties.containsKey(clazz)) {
			properties.put(clazz, properties(clazz));
		}
		Map<String, PropertyInterface> propertiesForClass = properties.get(clazz);
		return propertiesForClass;
	}
	
	public static boolean isReference(PropertyInterface property) {
		if (property.getFieldClazz().getName().startsWith("java")) return false;
		if (property.getFieldClazz().getName().startsWith("org.joda")) return false;
		if (Enum.class.isAssignableFrom(property.getFieldClazz())) return false;
		if (property.getAnnotation(Reference.class) != null) return true;
		return !property.isFinal();
	}
	
	public static boolean isRequired(PropertyInterface property) {
		return property.getAnnotation(Required.class) != null;
	}
	
	private static Map<String, PropertyInterface> properties(Class<?> clazz) {
		Map<String, PropertyInterface> properties = new LinkedHashMap<String, PropertyInterface>();
		
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || FieldUtils.isTransient(field)) continue;
			
			if (FieldUtils.isFinal(field) && !FieldUtils.isList(field)) {
				boolean isReference = field.getAnnotation(Reference.class) != null;
				if (!isReference) {
					Map<String, PropertyInterface> inlinePropertys = properties(field.getType());
					boolean hasClassName = ColumnPropertyUtils.hasClassName(field);
					for (String inlineKey : inlinePropertys.keySet()) {
						String key = inlineKey;
						if (!hasClassName) {
							key = field.getName() + StringUtils.upperFirstChar(inlineKey);
						}
						properties.put(key, new ChainedProperty(clazz, field, inlinePropertys.get(inlineKey)));
					}
				} else {
					properties.put(field.getName(), new FinalReferenceProperty(clazz, field));
				}
			} else {
				properties.put(field.getName(), new ColumnProperty(clazz, field));
			}
		}
		return properties; 
	}

	static class ColumnProperty implements PropertyInterface {
		private Field field;

		public ColumnProperty(Class<?> clazz, Field field) {
			this.field = field;
		}
		
		@Override
		public Class<?> getDeclaringClass() {
			return field.getDeclaringClass();
		}

		@Override
		public Object getValue(Object object) {
			try {
				return field.get(object);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setValue(Object object, Object value) {
			try {
				field.set(object, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getFieldName() {
			return field.getName();
		}

		@Override
		public String getFieldPath() {
			return getFieldName();
		}
		
		@Override
		public Type getType() {
			return field.getGenericType();
		}

		@Override
		public Class<?> getFieldClazz() {
			return field.getType();
		}
		
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return field.getAnnotation(annotationClass);
		}

		@Override
		public boolean isFinal() {
			return FieldUtils.isFinal(field);
		}
	}

	static class FinalReferenceProperty extends ColumnProperty {

		public FinalReferenceProperty(Class<?> clazz, Field field) {
			super(clazz, field);
		}

		@Override
		public void setValue(Object object, Object value) {
			Object finalValue = getValue(object);
			copy(value, finalValue);
		}
	}

	
	static class ChainedProperty implements PropertyInterface {
		private PropertyInterface next;
		private Field field;

		public ChainedProperty(Class<?> clazz, Field field, PropertyInterface next) {
			this.field = field;
			this.next = next;
		}

		@Override
		public Class<?> getDeclaringClass() {
			return next.getDeclaringClass();
		}

		@Override
		public Object getValue(Object object) {
			try {
				object = field.get(object);
				return next.getValue(object);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setValue(Object object, Object value) {
			try {
				object = field.get(object);
				next.setValue(object, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getFieldName() {
			return next.getFieldName();
		}

		@Override
		public String getFieldPath() {
			return field.getName() + "." + next.getFieldName();
		}

		@Override
		public Type getType() {
			return next.getType();
		}
		
		@Override
		public Class<?> getFieldClazz() {
			return next.getFieldClazz();
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return next.getAnnotation(annotationClass);
		}

		@Override
		public boolean isFinal() {
			return next.isFinal();
		}
	}
	
	public static <T> boolean equals(T o1, T o2) {
		Set<String> keys = ColumnProperties.getKeys(o1.getClass());
		for (String key : keys) {
			Object value1 = ColumnProperties.getValue(o1, key);
			Object value2 = ColumnProperties.getValue(o2, key);
			if (value1 == null && value2 != null || value1 != null && !value1.equals(value2)) return false;
		}
		return true;
	}
	
}
