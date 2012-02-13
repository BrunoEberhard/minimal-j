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
import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.ColumnAccess;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.StringUtils;

public class PropertyAccessor {
	private static final Logger logger = Logger.getLogger(PropertyAccessor.class.getName());

	private static final Map<Class<?>, Map<String, AccessorInterface>> accessors = 
			new HashMap<Class<?>, Map<String, AccessorInterface>>();
	
	/**
	 * Gets the value of an attribute of an Object.<p>
	 * 
	 * The attribute can be defined as public field or a pair of getter and setter methods
	 * 
	 * @param domainObject may be <code>null</code> in which case <code>null</code> is returned
	 * @param key the name of the attribute
	 * @return the value
	 */
	public static Object get(Object domainObject, String key) {
		if (domainObject == null) return null;
		if (key == null) return domainObject;
		
		int pos = key.lastIndexOf('.');
		if (pos > -1) {
			domainObject = get(domainObject, key.substring(0, pos));
			key = key.substring(pos + 1);
		}
		
		Class<?> clazz = domainObject.getClass();
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		
		AccessorInterface accessorInterface = accessorsForClass.get(key);
		if (accessorInterface != null) {
			return accessorInterface.getValue(domainObject);
		} else {
			logger.severe("No field/getMethod " + key + " in Class " + clazz.getName());
			return null;
		}
	}

	/**
	 * Sets the value of an attribute of an Object.<p>
	 * 
	 * The attribute can be defined as public field or a pair of getter and setter methods
	 * 
	 * @param domainObject may be <code>null</code> in which case nothing will be done
	 * @param key the name of the attribute
	 * @param value
	 */
	public static void set(Object domainObject, String key, Object value) {
		if (domainObject == null) return;

		int pos = key.lastIndexOf('.');
		if (pos > -1) {
			domainObject = get(domainObject, key.substring(0, pos));
			key = key.substring(pos + 1);
		}
		
		Class<?> clazz = domainObject.getClass();
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		
		AccessorInterface accessorInterface = accessorsForClass.get(key);
		if (accessorInterface != null) {
			accessorInterface.setValue(domainObject, value);
		} else {
			logger.severe("No field/setMethod " + key + " in Class " + clazz.getName());
		}
	}
	
	public static AccessorInterface getAccessor(Object domainObject, String key) {
		if (domainObject == null) throw new NullPointerException();

		int pos = key.lastIndexOf('.');
		if (pos > -1) {
			domainObject = get(domainObject, key.substring(0, pos));
			key = key.substring(pos + 1);
		}
		
		Class<?> clazz = domainObject.getClass();
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		
		AccessorInterface accessorInterface = accessorsForClass.get(key);
		if (accessorInterface != null) {
			return accessorInterface;
		} else {
			logger.severe("No field/setMethod " + key + " in Class " + clazz.getName());
			return null;
		}
	}

	public static AccessorInterface getAccessor(Class<?> clazz, String fieldName) {
		if (fieldName == null) throw new NullPointerException();

		int pos = fieldName.lastIndexOf('.');
		if (pos > -1) {
			clazz = getAccessor(clazz, fieldName.substring(0, pos)).getClazz();
			fieldName = fieldName.substring(pos + 1);
		}
		
		Map<String, AccessorInterface> accessorsForClass = getAccessors(clazz);
		
		AccessorInterface accessorInterface = accessorsForClass.get(fieldName);
		if (accessorInterface != null) {
			return accessorInterface;
		} else {
			logger.severe("No field/setMethod " + fieldName + " in Class " + clazz.getName());
			return null;
		}
	}

	private static Map<String, AccessorInterface> getAccessors(Class<?> clazz) {
		if (!accessors.containsKey(clazz)) {
			accessors.put(clazz, accessors(clazz));
		}
		Map<String, AccessorInterface> accessorsForClass = accessors.get(clazz);
		return accessorsForClass;
	}
	
	private static Map<String, AccessorInterface> accessors(Class<?> clazz) {
		Map<String, AccessorInterface> accessors = new LinkedHashMap<String, AccessorInterface>();
		
		for (Field field : clazz.getFields()) {
			if (FieldUtils.isPublic(field)) {
				accessors.put(field.getName(), new FieldAccessor(field));
			}
		}
		Method[] methods = clazz.getMethods();
		for (Method method: methods) {
			if (isPublic(method)) {
				String setterName = null;
				String name = method.getName();
				if (name.startsWith("get") && name.length() > 3 && method.getParameterTypes().length == 0) {
					setterName = "set" + method.getName().substring(3);
				} else if (name.startsWith("is") && name.length() > 2 && method.getParameterTypes().length == 0) {
					// TODO return should be boolean
					setterName = "set" + method.getName().substring(2);
				}
				if (setterName == null) continue;
				
				Method setterMethod = null;
				for (Method m: methods) {
					if (m.getName().equals(setterName)) {
						setterMethod = m;
						break;
					}
				}
				
				String key = setterName.substring(3, 4).toLowerCase();
				if (setterName.length() > 4) {
					key += setterName.substring(4);
				}
				accessors.put(key, new MethodAccessor(method, setterMethod));
			}
		}
		return accessors; 
	}
	
	private static boolean isPublic(Method method) {
		return Modifier.isPublic(method.getModifiers());
	}


	private static class FieldAccessor implements AccessorInterface {
		private final Field field;
		private final boolean isFinal;
		
		public FieldAccessor(Field field) {
			this.field = field;
			this.isFinal = FieldUtils.isFinal(field);
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
				if (!isFinal) {
					field.set(object, value);
				} else {
					Object finalObject = field.get(object);
					if (value == null) {
						value = EmptyObjects.getEmptyObject(field.getType());
					}
					ColumnAccess.copy(value, finalObject);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getName() {
			return field.getName();
		}

		@Override
		public Type getType() {
			return field.getGenericType();
		}

		@Override
		public Class<?> getClazz() {
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
	
	private static class MethodAccessor implements AccessorInterface {
		private final Method getterMethod;
		private final Method setterMethod;
		private final String name;

		public MethodAccessor(Method getterMethod, Method setterMethod) {
			this.getterMethod = getterMethod;
			this.setterMethod = setterMethod;
			this.name = StringUtils.lowerFirstChar(getterMethod.getName());
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
			try {
				setterMethod.invoke(object, value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type getType() {
			return getterMethod.getGenericReturnType();
		}

		@Override
		public Class<?> getClazz() {
			return getterMethod.getReturnType();
		}
		
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return getterMethod.getAnnotation(annotationClass);
		}

		@Override
		public boolean isFinal() {
			return setterMethod != null;
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
