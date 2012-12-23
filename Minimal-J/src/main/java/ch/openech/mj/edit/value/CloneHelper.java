package ch.openech.mj.edit.value;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.util.FieldUtils;

public class CloneHelper {

	public static <T> T clone(T object) {
		if (object == null) return null;

		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) object.getClass();
		try {
			T copy = newInstance(clazz);
			deepCopy(object, copy);
			return copy;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deepCopy(Object from, Object to) {
		try {
			_deepCopy(from, to);
		} catch (IllegalAccessException | IllegalArgumentException x) {
			throw new RuntimeException(x);
		}
	}
	
	private static void _deepCopy(Object from, Object to) throws IllegalArgumentException, IllegalAccessException {
		for (Field field : from.getClass().getDeclaredFields()) {
			if (FieldUtils.isStatic(field)) continue;
			field.setAccessible(true);
			Object fromValue = field.get(from);
			Object toValue = field.get(to);
			if (FieldUtils.isList(field.getType())) {
				List fromList = (List)field.get(from);
				if (fromList == null) continue;
				List toList = (List)toValue;
				if (!FieldUtils.isFinal(field)) {
					toList = new ArrayList();
					field.set(to, toList);
				}
				for (Object element : fromList) {
					toList.add(clone(element));
				}
			} else if (isPrimitive(field) || FieldUtils.isTransient(field) || fromValue == null) {
				// note: transient fields are not cloned
				field.set(to, fromValue);
			} else if (FieldUtils.isFinal(field) && toValue != null) {
				deepCopy(fromValue, toValue);
			} else {
				Object copyValue = CloneHelper.clone(fromValue);
				field.set(to, copyValue);
			}
		}
	}

	public static boolean isPrimitive(Field field) {
		if (field.getType().getName().startsWith("java")) return true;
		if (field.getType().getName().startsWith("org.joda")) return true;
		if (Enum.class.isAssignableFrom(field.getType())) return true;
		return false;
	}
	
	public static <T> T newInstance(Class<T> clazz) {
		try {
			Constructor<T> constructor = clazz.getConstructor();
			T newInstance = (T) constructor.newInstance();
			return newInstance;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
