package org.minimalj.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.StringUtils;

public class Keys {

	private static final Logger logger = Logger.getLogger(Keys.class.getName());
	private static final Map<Object, Property> properties = new IdentityHashMap<>();
	private static final Map<Property, List<Property>> dependencies = new HashMap<>();

	private static final HashSet<Object> keyObjects = new HashSet<>();
	private static final Map<Class<?>, Object> keyObjectByClass = new HashMap<>();
	private static final Map<Object, Map<String, Object>> methodKeys = new IdentityHashMap<>();
	
	/**
	 * 
	 * @param clazz The class the $ constant should be created and filled
	 * @param <T> Type of clazz itself (caller of this method doesn't need to care about this)
	 * @return the $ constant to be declared in business entities
	 */
	@SuppressWarnings("unchecked")
	public static <T> T of (Class<T> clazz) {
		synchronized(clazz) {
			T object = (T) keyObjectByClass.get(clazz); 
			if (object == null) {
				try {
					object = clazz.getConstructor().newInstance();
					keyObjects.add(object);
					keyObjectByClass.put(clazz, object);
					fillFields(object, null, 0);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
			return object;
		}
	}
	
	public static boolean isKeyObject(Object object) {
		return keyObjects.contains(object) || properties.containsKey(object);
	}

	@SuppressWarnings("unchecked")
	public static <T> T methodOf(Object keyObject, String propertyName, Object... dependencies) {
		Property enclosingProperty = null;
		if (properties.containsKey(keyObject)) {
			enclosingProperty = properties.get(keyObject);
		}
		
		Property property = getMethodProperty(keyObject.getClass(), propertyName);
		if (property == null) {
			if (propertyName.startsWith("get")) {
				throw new IllegalArgumentException("methodOf must be called with the property name. Not with the getter name");
			} else {
				throw new IllegalArgumentException("methodOf called with invalid property name");
			}
		}
		if (enclosingProperty != null) {
			property = new ChainedProperty(enclosingProperty, property);
		}

		Map<String, Object> keysForObject = methodKeys.computeIfAbsent(keyObject, k -> new HashMap<>());
		if (keysForObject.containsKey(propertyName)) {
			return (T) keysForObject.get(propertyName);
		}
		T t = (T)createKey(property.getClazz(), propertyName, null);
		keysForObject.put(propertyName, t);
		
		fillFields(t, property, 0);
		properties.put(t, property);

		//
		
		List<Property> dependenciesList = new ArrayList<>();
		if (dependencies != null && dependencies.length > 0) {
			for (Object d : dependencies) {
				Property dependency = Keys.getProperty(d);
				if (enclosingProperty != null) {
					dependency = new ChainedProperty(enclosingProperty, dependency);
				}
				dependenciesList.add(dependency);
			}
		}
		if (enclosingProperty != null) {
			dependenciesList.add(enclosingProperty);
			List<Property> enclosingDependencies = Keys.dependencies.get(enclosingProperty);
			if (enclosingDependencies != null) {
				dependenciesList.addAll(enclosingDependencies);
			}
		}
		if (!dependenciesList.isEmpty()) {
			Keys.dependencies.put(property, dependenciesList);
		}
		
		return t;
	}
	
	private static <T> void fillFields(T object, Property enclosingProperty, int depth) {
		Map<String, Property> propertiesOfObject = Properties.getProperties(object.getClass());
		for (Property property : propertiesOfObject.values()) {
			if (StringUtils.equals(property.getName(), "version", "historized"))
				continue;

			Object value = null;
			Class<?> clazz = property.getClazz();

			if (property.isFinal()) {
				value = property.getValue(object);
			} else {
				value = createKey(clazz, property.getName(), property.getDeclaringClass());
				property.setValue(object, value);	
			}
			
			if (enclosingProperty != null) {
				property = new ChainedProperty(enclosingProperty, property);
			}

			boolean fill = !clazz.getName().startsWith("java") && !clazz.isArray();
			if (fill && depth < 6) {
				fillFields(value, property, depth + 1);
			}
			
			properties.put(value, property);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object createKey(Class<?> clazz, String fieldName, Class<?> declaringClass) {
		if (clazz == String.class) {
			return new String(fieldName);
		} else if (clazz == Integer.class) {
			return new Integer(0);
		} else if (clazz == Long.class) {
			return new Long(0);
		} else if (Enum.class.isAssignableFrom(clazz)) {
			Class<Enum> enumClass = (Class<Enum>) clazz;
			return EnumUtils.createEnum(enumClass, fieldName);
		} else if (clazz == Boolean.class) {
			return new Boolean(false);
		} else if (clazz == BigDecimal.class) {
			return new BigDecimal(0);
		} else if (clazz == LocalDate.class) {
			return LocalDate.now();			
		} else if (clazz == LocalDateTime.class) {
			return LocalDateTime.now();	
		} else if (clazz == LocalTime.class) {
			return LocalTime.now();				
		} else if (clazz.isArray()) {
			return Array.newInstance(clazz.getComponentType(), 0);
		} else if (clazz == List.class) {
			return new ArrayList<>();			
		} else if (clazz.isAssignableFrom(String.class)) {
			return new String(fieldName);
		} else {
			try {
				Object keyObject = clazz.newInstance();
				keyObjects.add(keyObject);
				return keyObject;
			} catch (Exception x) {
				if (declaringClass != null) {
					logger.severe("Could not instantiate " + fieldName + " in class " + declaringClass);
				} else {
					logger.severe("Could not instantiate " + fieldName);				
				}
				return null;
			}
		}
	}
	
	public static Property getProperty(Object key) {
		if (key instanceof Property) {
			return (Property) key;
		} else {
			return properties.get(key);
		}
	}
	
	public static Property[] getProperties(Object[] keys) {
		Property[] properties = new Property[keys.length];
		for (int i = 0; i<keys.length; i++) {
			properties[i] = getProperty(keys[i]);
		}
		return properties;
	}
	
	public static List<Property> getDependencies(Property property) {
		return dependencies.getOrDefault(property, Collections.emptyList());
	}

	public static MethodProperty getMethodProperty(Class<?> clazz, String methodName) {
		Method[] methods = clazz.getMethods();
		for (Method method: methods) {
			// TODO the last condition should not be based on super method returns abstract class
			// somehow the method in the extension class should win
			// the array check is because of char[]  . char[] is reported as abstract
			if (isStatic(method) || !isPublic(method) || Modifier.isAbstract(method.getModifiers()) || //
					method.getParameterCount() > 0 || //
					Modifier.isAbstract(method.getReturnType().getModifiers()) && //
					!method.getReturnType().isArray() && //
					!Collection.class.isAssignableFrom(method.getReturnType())) continue;
			String name = method.getName();
			if (!name.startsWith("get") || name.length() <= 3) continue; // TODO check
			if (!StringUtils.lowerFirstChar(name.substring(3)).equals(methodName)) continue;
			
			String setterName = "set" + name.substring(3);
			Method setterMethod = null;
			for (Method m: methods) {
				if (m.getName().equals(setterName)) {
					setterMethod = m;
					break;
				}
			}
			return new MethodProperty(method.getDeclaringClass(), methodName, method, setterMethod);
		}
		return null;
	}
	
	public static class MethodProperty implements Property {
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
		public String getName() {
			return name;
		}

		@Override
		public String getPath() {
			return getName();
		}

		public Class<?> getGenericClass() {
			Type genericType = getterMethod.getGenericReturnType();
			return genericType != null ? GenericUtils.getGenericClass(genericType) : null;
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
			return setterMethod == null;
		}
		
		@Override
		public String toString() {
			return "[" + getterMethod.getDeclaringClass().getSimpleName() + "." + getterMethod.getName() + "]";
		}
	}
	
	// TODO merge with FieldUtils.isPublic
	public static boolean isPublic(Method method) {
		return Modifier.isPublic(method.getModifiers());
	}
	
	public static boolean isStatic(Method method) {
		return Modifier.isStatic(method.getModifiers());
	}
	
	// Arrays.equals doesn't work for Keys as it uses equals.
	// Different Boolean keys would always be equals
	public static boolean equalsIdentity(Object[] keys1, Object[] keys2) {
		if (keys1.length != keys2.length) {
			return false;
		}
		for (int i = 0; i < keys1.length; i++) {
			if (keys1[i] != keys2[i]) {
				return false;
			}
		}
		return true;
	}

}
