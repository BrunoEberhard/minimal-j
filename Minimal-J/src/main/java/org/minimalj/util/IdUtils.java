package org.minimalj.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class IdUtils {
	
	private static final Logger logger = Logger.getLogger(IdUtils.class.getName());
	private static final Map<Class<?>, Field> idFieldOfClass = new HashMap<>(200);
	
	private static Field getIdField(Class<?> clazz) {
		if (idFieldOfClass.containsKey(clazz)) {
			return idFieldOfClass.get(clazz);
		}
		try {
			Field idField = clazz.getField("id");
			idFieldOfClass.put(clazz, idField);
			return idField;
		} catch (NoSuchFieldException e) {
			idFieldOfClass.put(clazz, null);
			return null;
		} catch (SecurityException e) {
			throw new LoggingRuntimeException(e, logger, "getting Id failed");
		}
	}
	
	public static Object getId(Object object) {
		if (object != null) {
			try {
				Field idField = getIdField(object.getClass());
				if (idField == null) throw new IllegalArgumentException(object.getClass().getName() + " has no id field to get");
				Object id = idField.get(object);
				return id;
			} catch (SecurityException | IllegalAccessException e) {
				throw new LoggingRuntimeException(e, logger, "getting Id failed");
			}
		} else {
			throw new IllegalArgumentException("object must not be null");
		}
	}
	
	public static void setIdSafe(Object object, Object id) {
		try {
			Field idField = getIdField(object.getClass());
			if (idField != null) {
				idField.set(object, id);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new LoggingRuntimeException(e, logger, "setting Id failed");
		}
	}
	
	public static void setId(Object object, Object id) {
		try {
			Field idField = getIdField(object.getClass());
			if (idField != null) {
				idField.set(object, id);
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new LoggingRuntimeException(e, logger, "setting Id failed");
		}
	}

	public static void setVersion(Object object, int id) {
		try {
			Field versionField = object.getClass().getField("version");
			if (versionField.getType() == Integer.TYPE) versionField.set(object, (int) id);
			else throw new IllegalArgumentException("Cannot set version on field with " + versionField.getType());
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			// throw new LoggingRuntimeException(e, logger, "setting Version failed");
		}
	}

	public static String getIdString(Object object) {
		return String.valueOf(getId(object));
	}

	public static long convertToLong(Object value) {
		if (value instanceof Integer) {
			return ((Integer) value).longValue();
		} else if (value instanceof Long) {
			return ((Long) value).longValue();
		} else if (value instanceof Short) {
			return ((Short) value).longValue();
		} else if (value instanceof Byte) {
			return ((Byte) value).longValue();
		} else {
			return 0;
		}
	}
}
