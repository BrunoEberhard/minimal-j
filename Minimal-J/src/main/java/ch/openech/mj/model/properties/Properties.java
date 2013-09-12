package ch.openech.mj.model.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ch.openech.mj.db.EmptyObjects;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.FieldUtils;

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
				properties.put(field.getName(), new FieldProperty(clazz, field));
			} 
		}
		return properties; 
	}
	
	private static class FieldProperty implements PropertyInterface {
		private final Class<?> clazz;
		private final Field field;
		private final boolean isFinal;
		
		public FieldProperty(Class<?> clazz, Field field) {
			this.clazz = clazz;
			this.field = field;
			this.isFinal = FieldUtils.isFinal(field);
		}

		@Override
		public Class<?> getDeclaringClass() {
			return clazz;
		}

		@Override
		public Object getValue(Object object) {
			try {
				return field.get(object);
			} catch (Exception e) {
				System.out.println("E: " + e.getLocalizedMessage());
				System.out.println("O: " + object + (object != null ? "  (" + object.getClass() +")" : ""));
				System.out.println("F: " + field.getName() + " (" + field.getType() + ")");
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setValue(Object object, Object value) {
			try {
				if (!isFinal) {
					field.set(object, value);
				} else {
					Object finalObject = field.get(object);
					if (finalObject == value) return;
					if (finalObject instanceof List) {
						List<?> finalList = (List<?>) finalObject;
						finalList.clear();
						if (value != null) {
							finalList.addAll((List) value);
						}
					} else {
						if (value == null) {
							value = EmptyObjects.getEmptyObject(field.getType());
						}
						CloneHelper.deepCopy(value, finalObject);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
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
			return isFinal;
		}
	}
	
}
