package org.minimalj.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.Property;

public class FieldUtils {
	public static final Logger logger = Logger.getLogger(FieldUtils.class.getName());

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
	
	public static boolean isAllowedPrimitive(Class<?> fieldType) {
		if (String.class == fieldType) return true;
		if (Integer.class == fieldType) return true;
		if (Long.class == fieldType) return true;
		if (Boolean.class == fieldType) return true;
		if (BigDecimal.class == fieldType) return true;
		if (LocalDate.class == fieldType) return true;
		if (LocalTime.class == fieldType) return true;
		if (LocalDateTime.class == fieldType) return true;
		if (fieldType.isArray() && fieldType.getComponentType() == Byte.TYPE) return true;
		return false;
	}

	public static boolean isAllowedId(Class<?> classOfId) {
		if (Integer.class == classOfId) return true;
		if (String.class == classOfId) return true;
		if (Object.class == classOfId) return true;
		if (Long.class == classOfId) return true;
		return false;
	}
	
	public static boolean isAllowedVersionType(Class<?> classOfId) {
		return classOfId == Integer.TYPE || classOfId == Long.TYPE;
	}
	
	public static boolean hasValidVersionfield(Class<?> clazz) {
		try {
			Field field = clazz.getField("version");
			if (isAllowedVersionType(field.getType())) {
				return true;
			} else {
				throw new RuntimeException("Type of version field invalid: " + field.getType());
			}
		} catch (NoSuchFieldException e) {
			return false;
		} catch (SecurityException e) {
			throw new LoggingRuntimeException(e, logger, "hasValidVersionfield failed");
		}
	}
	
	public static boolean hasValidHistorizedField(Class<?> clazz) {
		try {
			Field field = clazz.getField("historized");
			if (field.getType() == Boolean.TYPE) {
				return true;
			} else {
				throw new RuntimeException("Type of historized field invalid: " + field.getType());
			}
		} catch (NoSuchFieldException e) {
			return false;
		} catch (SecurityException e) {
			throw new LoggingRuntimeException(e, logger, "hasValidHistorizedField failed");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getStaticValue(Field field) {
		try {
			return (T) field.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param <T> type (class) of result
	 * @param s input string (may be null or empty)
	 * @param clazz the target class
	 * @return null or the parsed input. If class is a time class the iso format is used (not the locale format)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T parse(String s, Class<T> clazz) {
		Object value = null;
		if (clazz == String.class) {
			value = s;
		} else if (!StringUtils.isEmpty(s)) {
			if (clazz == Integer.class) {
				value = Integer.valueOf(s);
			} else if (clazz == Long.class) {
				value = Long.valueOf(s);
			} else if (clazz == Boolean.class) {
				value = Boolean.valueOf(s);
			} else if (clazz == BigDecimal.class) {
				value = new BigDecimal(s);
			} else if (clazz == LocalDate.class) {
				if (s.length() > 10) s = s.substring(0, 10);
				value = LocalDate.parse(s);
			} else if (clazz == LocalTime.class) {
				value = LocalTime.parse(s);
			} else if (clazz == LocalDateTime.class) {
				value = LocalDateTime.parse(s);
			} else if (clazz.isEnum()) {
				List<Enum> values = EnumUtils.valueList((Class<Enum>) clazz);
				for (Enum enumValue : values) {
					String enumName = enumValue.name();
					if (enumName.startsWith("_")) {
						enumName = enumName.substring(1);
					}
					if (enumName.equalsIgnoreCase(s)) {
						value = enumValue;
						break;
					}
				}
			} else if (clazz.isArray() && clazz.getComponentType() == Byte.TYPE) {
				value = Base64.getDecoder().decode(s);
			} else {
				throw new IllegalArgumentException(clazz.getSimpleName() + ": " + s);
			}
		}
		return (T) value;
	}
	
	public static <T> void swap(Object object, T key1, T key2) {
		if (key1 == key2) {
			throw new IllegalArgumentException();
		}
		Property property1 = Keys.getProperty(key1);
		Property property2 = Keys.getProperty(key2);
		Object value = property1.getValue(object);
		property1.setValue(object, property2.getValue(object));
		property2.setValue(object, value);
	}

}
