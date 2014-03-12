package ch.openech.mj.util;

import java.lang.reflect.Field;
import java.util.logging.Logger;


public class IdUtils {
	
	private static final Logger logger = Logger.getLogger(IdUtils.class.getName());

	public static long getId(Object object) {
		if (object != null) {
			try {
				Field idField = object.getClass().getField("id");
				Object id = idField.get(object);
				if (id instanceof Byte) return Long.valueOf((Byte) id);
				else if (id instanceof Short) return Long.valueOf((Short) id);
				else if (id instanceof Integer) return Long.valueOf((Integer) id);
				else if (id instanceof Long) return Long.valueOf((Long) id);
				throw new IllegalArgumentException("Cannot convert id: " + object);
			} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
				throw new LoggingRuntimeException(e, logger, "getting Id failed");
			}
		} else {
			throw new IllegalArgumentException("object must not be null");
		}
	}
	
	public static void setId(Object object, long id) {
		try {
			Field idField = object.getClass().getField("id");
			if (idField.getType() == Byte.TYPE) idField.set(object, (byte) id);
			else if (idField.getType() == Short.TYPE) idField.set(object, (short) id);
			else if (idField.getType() == Integer.TYPE) idField.set(object, (int) id);
			else if (idField.getType() == Long.TYPE) idField.set(object, (long) id);
			else throw new IllegalArgumentException("Cannot set id on field with " + idField.getType());
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			throw new LoggingRuntimeException(e, logger, "setting Id failed");
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
