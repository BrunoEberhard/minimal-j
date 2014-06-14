package org.minimalj.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class GenericUtils {

	public static Class<?> getGenericClass(Class<?> c) {
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
		return getClass(type.getActualTypeArguments()[0]);
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
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
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
		return (Class<?>) type.getActualTypeArguments()[0];
	}

	public static Class<?> getGenericClass(Field field) {
		Type type = field.getGenericType();
		if (!(type instanceof ParameterizedType)) {
			throw new RuntimeException("Unable to evaluate Generic class of " + field);
		}
		ParameterizedType parameterizedType = (ParameterizedType) type;
		return (Class<?>) parameterizedType.getActualTypeArguments()[0];
	}
}
