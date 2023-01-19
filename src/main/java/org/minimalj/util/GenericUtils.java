package org.minimalj.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class GenericUtils {
	private static final Logger logger = Logger.getLogger(GenericUtils.class.getName());
	
	public static Class<?> getGenericClass(Class<?> c) {
		return getGenericClass(c, 0);
	}
	
	public static Class<?> getGenericClass(Class<?> c, int index) {
		ParameterizedType type = null;
		while (c != Object.class) {
			Type genericSuperclass = c.getGenericSuperclass();
			if (!(genericSuperclass instanceof ParameterizedType)) {
				c = c.getSuperclass();
			} else {
				type = (ParameterizedType) genericSuperclass;
				break;
			}
		}
		if (type == null) {
			throw new IllegalArgumentException(c.toString() + " must be parameterized!");
		}
		Type[] actualTypeArguments = type.getActualTypeArguments();
		return actualTypeArguments.length > index ? getClass(actualTypeArguments[index]) : null;
	}
	
	/**
	 * Get the underlying class for a type, or null if the type is a variable
	 * type.
	 * 
	 * @param type the type
	 * @return the underlying class or <code>null</code>
	 */
	public static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			}
		}
		return null;
	}
	
	public static boolean declaresInterface(Class<?> clazz, Class<?> interfce) {
		for (Class<?> i : clazz.getInterfaces()) {
			if (i == interfce) return true;
		}
		return false;
	}
	
	/**
	 * Get the actual type argument for a interface with one type
	 * 
	 * @param clazz the class implementing the interface (directly or by extension)
	 * @param interfce the interface implemented by clazz. Must have exactly one type
	 * @return the class the interface had in type argument
	 */
	public static Class<?> getTypeArgument(Class<?> clazz, Class<?> interfce) {
		Map<Type, Type> resolvedTypes = new HashMap<>();
		Type type = clazz;
		
		while (!declaresInterface(getClass(type), interfce)) {
			if (type instanceof Class) {
				type = ((Class<?>) type).getGenericSuperclass();
			} else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> rawType = (Class<?>) parameterizedType.getRawType();

				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++) {
					resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
				}

				type = rawType.getGenericSuperclass();
			}
		}

		Type actualTypeArgument;
		if (type instanceof Class) {
			actualTypeArgument = ((Class<?>) type).getTypeParameters()[0];
		} else {
			actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
		}
		Class<?> typeArgumentAsClass = null;
		
		while (resolvedTypes.containsKey(actualTypeArgument)) {
			actualTypeArgument = resolvedTypes.get(actualTypeArgument);
		}
		typeArgumentAsClass = getClass(actualTypeArgument);
		return typeArgumentAsClass;
	}
	  
	public static Class<?> getGenericClass(Type genericSuperclass) {
		if (!(genericSuperclass instanceof ParameterizedType)) {
			throw new IllegalArgumentException(genericSuperclass.toString() + " must be parameterized!");
		}
		ParameterizedType type = (ParameterizedType) genericSuperclass;
		Type actualTypeArgument = type.getActualTypeArguments()[0];
		while (actualTypeArgument instanceof ParameterizedType) {
			actualTypeArgument = ((ParameterizedType) actualTypeArgument).getRawType();
		}
		return (Class<?>) actualTypeArgument;
	}

	/**
	 * 
	 * @param field field of a class
	 * @return generic class or <code>null</code>. Doesn't throw Exception if field
	 *         is not generic.
	 */
	public static Class<?> getGenericClass(Field field) {
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
			return actualTypeArgument instanceof Class ? (Class<?>) actualTypeArgument : null;
		}
		return null;
	}
	
	public static Class<?> getGenericClass(Class<?> inClass, Field field) {
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
			if (actualTypeArgument instanceof Class) {
				return (Class<?>) actualTypeArgument;
			} else if (actualTypeArgument instanceof TypeVariable) {
				TypeVariable typeVariable = (TypeVariable) actualTypeArgument;
				return getTypeVariableValue(typeVariable.getName(), field.getDeclaringClass(), inClass, Collections.emptyMap());
			}
		}
		return null;
	}
	
	private static Class<?> getTypeVariableValue(String name, Class<?> declaringClass, Class<?> inClass, Map<String, Class<?>> names) {
		if (declaringClass == inClass) {
			return names.get(name);
		}
		
		ParameterizedType ptc = (ParameterizedType) inClass.getGenericSuperclass();
		Type[] actualTypeArguments = ptc.getActualTypeArguments();
		TypeVariable<?>[] typeParameters = ((Class<?>) ptc.getRawType()).getTypeParameters();

		Map<String, Class<?>> thisNames = new HashMap<>();
		for (int i = 0; i<actualTypeArguments.length; i++) {
			String typeParameterName = typeParameters[i].getName();
			Type actualTypeArgument = actualTypeArguments[i];
			if (actualTypeArgument instanceof Class) {
				thisNames.put(typeParameterName, (Class<?>) actualTypeArgument);
			} else if (actualTypeArgument instanceof TypeVariable) {
				TypeVariable typeVariable = (TypeVariable) actualTypeArgument;
				Class<?> clazz = names.get(typeVariable.getName());
				thisNames.put(typeParameterName, clazz);
			} else {
				throw new IllegalArgumentException(actualTypeArgument.toString());
			}
		}
		
		return getTypeVariableValue(name, declaringClass, (Class<?>) ptc.getRawType(), thisNames);
	}
	
}
