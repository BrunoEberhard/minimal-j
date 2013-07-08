package ch.openech.mj.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

public class FieldUtils {

	public static boolean isPublic(Field field) {
		return Modifier.isPublic(field.getModifiers());
	}

	public static boolean isFinal(Field field) {
		return Modifier.isFinal(field.getModifiers());
	}

	public static boolean isStatic(Field field) {
		return Modifier.isStatic(field.getModifiers());
	}

	public static boolean isTransient(Field field) {
		return Modifier.isTransient(field.getModifiers());
	}

	public static boolean isAbstract(Field field) {
		return Modifier.isAbstract(field.getModifiers());
	}
	
	public static boolean isInterface(Field field) {
		return Modifier.isInterface(field.getModifiers());
	}
	
	
	public static boolean isList(Field field) {
		return isList(field.getType());
	}
	
	public static boolean isList(Class<?> clazz) {
		return List.class.isAssignableFrom(clazz);
	}

	public static boolean isSet(Field field) {
		return isSet(field.getType());
	}
	
	public static boolean isSet(Class<?> clazz) {
		return Set.class.isAssignableFrom(clazz);
	}

	public static boolean hasClassName(Field field) {
		String fieldName = field.getName();
		String className = field.getType().getSimpleName();
		return sameFirstChar(fieldName, className) && equalsBeginningSecondChar(fieldName, className);
	}

	private static boolean equalsBeginningSecondChar(String fieldName, String className) {
		if (fieldName.length() == 1 && className.length() == 1)
			return true;
		return fieldName.substring(1).equals(className.substring(1));
	}

	private static boolean sameFirstChar(String fieldName, String className) {
		char fieldFirstChar = fieldName.charAt(0);
		char classFirstChar = className.charAt(0);
		return Character.toUpperCase(fieldFirstChar) == classFirstChar;
	}
	
	public static Field getValueField(Class<?> clazz) {
		for (Field field : clazz.getFields()) {
			if (FieldUtils.isFinal(field) || FieldUtils.isStatic(field) || FieldUtils.isTransient(field) || !FieldUtils.isPublic(field)) continue;
			if (field.getDeclaringClass() != clazz) continue;
			return field;
		}
		throw new IllegalArgumentException("Class should have at least one Field: " + clazz.getName());
	}
	
	public static void setValue(Object object, Object value) {
		try {
			getValueField(object.getClass()).set(object, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Object getValue(Object object) {
		try {
			return getValueField(object.getClass()).get(object);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
