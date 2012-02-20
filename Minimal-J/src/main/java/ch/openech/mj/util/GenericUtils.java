package ch.openech.mj.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
		return (Class<?>) type.getActualTypeArguments()[0];
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
