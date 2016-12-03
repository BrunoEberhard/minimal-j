package org.minimalj.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Every main entity in a minimal-j model must have a public field named <code>id</code>.<p>
 *
 * This class provides get and set for this special field. It caches the
 * reflection calls. Mostly it's used for minimal-j internal stuff.
 */
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

	/**
	 * Check if a class has an <code>id</code> field
	 * 
	 * @param clazz class to check. Must not be <code>null</code>
	 * @return true if the given class has a field named <code>id</code>
	 */
	public static boolean hasId(Class<?> clazz) {
		return getIdField(clazz) != null;
	}

	/**
	 * Get the value of the <code>id</code> field. The id is converted to
	 * 'plain' if it is a ReadOnly id
	 * 
	 * @param object object containing the id. Must not be <code>null</code>
	 * @return the value of the <code>id</code> field
	 */
	public static Object getId(Object object) {
		Objects.nonNull(object);
		try {
			Field idField = getIdField(object.getClass());
			if (idField == null) throw new IllegalArgumentException(object.getClass().getName() + " has no id field to get");
			Object id = idField.get(object);
			return id;
		} catch (SecurityException | IllegalAccessException e) {
			throw new LoggingRuntimeException(e, logger, "getting Id failed");
		}
	}
	
	public static boolean equals(Object a, Object b) {
		if (a != null && hasId(a.getClass())) {
			a = getId(a);
		}
		if (b != null && hasId(b.getClass())) {
			b = getId(b);
		}
		return Objects.equals(a, b);
	}
	
	/**
	 * Set the value of the <code>id</code> in the given object
	 * 
	 * @param object object containing a public <code>id</code> field. Must not be <code>null</code>
	 * @param id the new value. Can be <code>null</code>.
	 */
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

	/**
	 * Set the value of the <code>version</code> in the given object
	 * 
	 * @param object object containing a public <code>version</code> field. Must not be <code>null</code>
	 * @param id the new value. Can be <code>null</code>.
	 */
	public static void setVersion(Object object, int id) {
		try {
			Field versionField = object.getClass().getField("version");
			if (versionField.getType() == Integer.TYPE) versionField.set(object, id);
			else throw new IllegalArgumentException("Cannot set version on field with " + versionField.getType());
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static int getVersion(Object object) {
		try {
			Field versionField = object.getClass().getField("version");
			if (versionField.getType() == Integer.TYPE) return (Integer) versionField.get(object);
			else throw new IllegalArgumentException("Cannot set version on field with " + versionField.getType());
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get the value of the <code>id</code> field as String
	 * 
	 * @param object object containing the id. Must not be <code>null</code>
	 * @return the value of the <code>id</code> field as String
	 */
	public static String getIdString(Object object) {
		if (object == null) {
			throw new NullPointerException();
		}
		return String.valueOf(getId(object));
	}

	/**
	 * Get the value of the <code>id</code> field as String.
	 * Leaves out all 'filler' characters. For an UUID this would
	 * be the '-' characters
	 * 
	 * @param object object containing the id. Must not be <code>null</code>
	 * @return the value of the <code>id</code> field as String
	 */
	public static String getCompactIdString(Object object) {
		return getIdString(object).replace("-", "");
	}
	
	/**
	 * Note: Do not depend on the class of the returned id. It
	 * could be changed. 
	 * 
	 * @return a new unique id
	 */
	public static Object createId() {
		return UUID.randomUUID().toString();
	}

}
