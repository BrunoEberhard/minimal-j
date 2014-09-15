package org.minimalj.util;

import java.lang.reflect.Field;
import java.util.logging.Logger;


public class IdUtils {
	
	private static final Logger logger = Logger.getLogger(IdUtils.class.getName());

	public static Object getId(Object object) {
		if (object != null) {
			try {
				Field idField = object.getClass().getField("id");
				Object id = idField.get(object);
				return id;
			} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
				throw new LoggingRuntimeException(e, logger, "getting Id failed");
			}
		} else {
			throw new IllegalArgumentException("object must not be null");
		}
	}
	
	public static void setId(Object object, Object id) {
		try {
			Field idField = object.getClass().getField("id");
			idField.set(object, id);
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			// throw new LoggingRuntimeException(e, logger, "setting Id failed");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
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
