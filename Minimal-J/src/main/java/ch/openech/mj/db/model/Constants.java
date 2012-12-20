package ch.openech.mj.db.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Logger;

import ch.openech.mj.edit.value.Properties;
import ch.openech.mj.edit.value.Properties.MethodProperty;

public class Constants {

	private static final Logger logger = Logger.getLogger(Constants.class.getName());
	private static final Map<Object, PropertyInterface> properties = new IdentityHashMap<Object, PropertyInterface>();
	
	/**
	 * Warning: Should only be call once per class
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> T of (Class<T> clazz) {
		T object;
		try {
			object = clazz.newInstance();
			fillFields(object, null, 0);
			return object;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> void fillFields(T object, PropertyInterface enclosingProperty, int depth) throws IllegalAccessException, InstantiationException {
		for (Map.Entry<String, PropertyInterface> entry : Properties.getProperties(object.getClass()).entrySet()) {
			PropertyInterface property = entry.getValue();
			
			Object value = null;
			Class<?> type = property.getFieldClazz();

			if (property.isFinal()) {
				value = property.getValue(object);
			} else {
				if (type == String.class) {
					value = new String(property.getFieldName());
				} else if (type == Integer.class) {
					value = new Integer(0);
				} else if (Enum.class.isAssignableFrom(type)) {
					Class<Enum> enumClass = (Class<Enum>) type;
					value = EnumUtils.createEnum(enumClass, property.getFieldName());
				} else if (type == Boolean.class) {
					value = new Boolean(false);
				} else if (type == BigDecimal.class) {
					value = new BigDecimal(0);
				} else {
					// note: LocalDate, LocaleDateTime etc have an empty constructor
					// so they are constructed in the else branch
					try {
						value = type.newInstance();
					} catch (Exception x) {
						logger.severe("Could not instantiat " + property.getFieldName() + " in class " + property.getDeclaringClass());
						continue;
					}
				}
				property.setValue(object, value);	
			}
			
			if (enclosingProperty != null) {
				property = new ChainedProperty(enclosingProperty, property);
			}

			boolean fill = !type.getName().startsWith("java") && !type.getName().startsWith("org.joda");
			if (fill && depth < 6 && !(property instanceof MethodProperty)) {
				fillFields(value, property, depth + 1);
			}
			
			properties.put(value, property);
		}
	}
	
	public static PropertyInterface getProperty(Object key) {
		return properties.get(key);
	}
	
	static class ChainedProperty implements PropertyInterface {
		private final PropertyInterface property1;
		private final PropertyInterface property2;

		public ChainedProperty(PropertyInterface property1, PropertyInterface property2) {
			this.property1 = property1;
			this.property2 = property2;
		}
		
		@Override
		public Class<?> getDeclaringClass() {
			return property2.getDeclaringClass();
		}

		@Override
		public Object getValue(Object object) {
			Object value1 = property1.getValue(object);
			Object value2 = property2.getValue(value1);
			return value2;
		}

		@Override
		public void setValue(Object object, Object value) {
			Object value1 = property1.getValue(object);
			property2.setValue(value1, value);
		}

		@Override
		public String getFieldName() {
			return property2.getFieldName();
		}

		@Override
		public Type getType() {
			return property2.getType();
		}
		
		@Override
		public Class<?> getFieldClazz() {
			return property2.getFieldClazz();
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return property2.getAnnotation(annotationClass);
		}

		@Override
		public boolean isFinal() {
			return property2.isFinal();
		}
	}

	public static PropertyInterface[] getProperties(Object[] keys) {
		PropertyInterface[] properties = new PropertyInterface[keys.length];
		for (int i = 0; i<keys.length; i++) {
			properties[i] = getProperty(keys[i]);
		}
		return properties;
	}

}
