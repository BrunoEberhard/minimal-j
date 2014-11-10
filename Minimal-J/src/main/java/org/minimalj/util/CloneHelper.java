package org.minimalj.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CloneHelper {

	/*
	 * With all the security checks getting a constructor is quite expensive.
	 * So here is a cache for the constructors for the already cloned classes.
	 */
	private static Map<Class<?>, Constructor<?>> contructors = new HashMap<>(200);
	
	/**
	 * note: clone works only with special classes validatable through
	 * ModelTest . 
	 * 
	 */
	public static <T> T clone(T object) {
		if (object == null) return null;

		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) object.getClass();
		try {
			T copy = newInstance(clazz);
			_deepCopy(object, copy);
			return copy;
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * note: deepCopy works only with special classes validatable through
	 * ModelTest . 
	 * 
	 */
	public static void deepCopy(Object from, Object to) {
		if (from == null) throw new IllegalArgumentException("from must not be null");
		if (to == null) throw new IllegalArgumentException("to must not be null");
		if (from.getClass() != to.getClass()) throw new IllegalArgumentException("from and to must have exactly same class, from has " + from.getClass() + " to has " + to.getClass());

		try {
			_deepCopy(from, to);
		} catch (IllegalAccessException | IllegalArgumentException x) {
			throw new RuntimeException(x);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void _deepCopy(Object from, Object to) throws IllegalArgumentException, IllegalAccessException {
		for (Field field : from.getClass().getDeclaredFields()) {
			if (FieldUtils.isStatic(field)) continue;
			field.setAccessible(true);
			Object fromValue = field.get(from);
			Object toValue = field.get(to);
			if (fromValue instanceof List) {
				List fromList = (List)field.get(from);
				List toList = (List)toValue;
				toList.clear();
				for (Object element : fromList) {
					toList.add(clone(element));
				}
			} else if (fromValue instanceof Set) {
				Set fromSet = (Set)field.get(from);
				Set toSet = (Set)toValue;
				toSet.clear();
				toSet.addAll(fromSet);
			} else if (isPrimitive(field) || FieldUtils.isTransient(field) || fromValue == null) {
				// note: transient fields are not cloned
				field.set(to, fromValue);
			} else if (fromValue != null && toValue == null) {
				toValue = CloneHelper.clone(fromValue);
				field.set(to, toValue);
			} else {
				deepCopy(fromValue, toValue);
			}
		}
	}

	public static boolean isPrimitive(Field field) {
		String fieldTypeName = field.getType().getName();
		if (field.getType().isPrimitive() || fieldTypeName.startsWith("java")) return true;
		if (Enum.class.isAssignableFrom(field.getType())) return true;
		return false;
	}
	
	public static <T> T newInstance(Class<T> clazz) {
		@SuppressWarnings("unchecked")
		Constructor<T> constructor = (Constructor<T>) contructors.get(clazz);
		if (constructor == null) {
			try {
				constructor = clazz.getConstructor();
				contructors.put(clazz, constructor);
			} catch (SecurityException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			T newInstance = (T) constructor.newInstance();
			return newInstance;
		} catch (IllegalArgumentException | //
				InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
