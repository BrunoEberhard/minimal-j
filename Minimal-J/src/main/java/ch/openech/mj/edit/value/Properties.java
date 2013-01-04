package ch.openech.mj.edit.value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import ch.openech.mj.db.EmptyObjects;
import ch.openech.mj.db.model.ColumnProperties;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.StringUtils;

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
	
	public static MethodProperty getMethodProperty(Class<?> clazz, String methodName) {
		Method[] methods = clazz.getMethods();
		for (Method method: methods) {
			if (isStatic(method) || !isPublic(method) || method.getDeclaringClass() != clazz) continue;
			String name = method.getName();
			if (!name.startsWith("get") && name.length() > 3) continue;
			if (!StringUtils.lowerFirstChar(name.substring(3)).equals(methodName)) continue;
			
			String setterName = "set" + name.substring(3);
			Method setterMethod = null;
			for (Method m: methods) {
				if (m.getName().equals(setterName)) {
					setterMethod = m;
					break;
				}
			}
			return new MethodProperty(clazz, methodName, method, setterMethod);
		}
		return null;
	}
	
	private static boolean isPublic(Method method) {
		return Modifier.isPublic(method.getModifiers());
	}
	
	private static boolean isStatic(Method method) {
		return Modifier.isStatic(method.getModifiers());
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
					if (value == null) {
						value = EmptyObjects.getEmptyObject(field.getType());
					}
					ColumnProperties.copy(value, finalObject);
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
	
	public static class MethodProperty implements PropertyInterface {
		private final Class<?> clazz;
		private final Method getterMethod;
		private final Method setterMethod;
		private final String name;

		public MethodProperty(Class<?> clazz, String key, Method getterMethod, Method setterMethod) {
			if (getterMethod == null) throw new IllegalArgumentException();

			this.clazz = clazz;
			this.getterMethod = getterMethod;
			this.setterMethod = setterMethod;
			this.name = key;
		}

		@Override
		public Class<?> getDeclaringClass() {
			return clazz;
		}

		@Override
		public Object getValue(Object object) {
			try {
				return getterMethod.invoke(object);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void setValue(Object object, Object value) {
			if (setterMethod == null) {
				logger.severe("No setter method for " + getterMethod.getName() + " on " + getterMethod.getDeclaringClass().getName());
				return;
			}
			try {
				setterMethod.invoke(object, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getFieldName() {
			return name;
		}

		@Override
		public String getFieldPath() {
			return getFieldName();
		}

		@Override
		public Type getType() {
			return getterMethod.getGenericReturnType();
		}

		@Override
		public Class<?> getFieldClazz() {
			return getterMethod.getReturnType();
		}
		
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return getterMethod.getAnnotation(annotationClass);
		}

		@Override
		public boolean isFinal() {
			return setterMethod == null;
		}
	}
	
	//

	public static Object newInstance(Object object, String key) {
		try {
			Field field = object.getClass().getField(key);
			return newInstance(field.getType());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}
		
	public static <T> T newInstance(Class<T> class1) {
		try {
			return class1.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
