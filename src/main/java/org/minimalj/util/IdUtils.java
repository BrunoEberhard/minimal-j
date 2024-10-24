package org.minimalj.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
	 * Check if a class has an <code>id</code> field. Cached.
	 * 
	 * @param clazz class to check. Must not be <code>null</code>
	 * @return true if the given class has a field named <code>id</code>
	 */
	public static boolean hasId(Class<?> clazz) {
		return getIdField(clazz) != null;
	}

	/**
	 * Cached getter for id class of an entity.
	 * 
	 * @param clazz the entity class
	 * @return the clazz of the id field. <code>null</code> if entity class has no id field.
	 */
	public static Class<?> getIdClass(Class<?> clazz) {
		Field field = getIdField(clazz);
		return field != null ?  field.getType() : null;
	}
	
	/**
	 * Get the value of the <code>id</code> field. The id is converted to
	 * 'plain' if it is a ReadOnly id
	 * 
	 * @param object object containing the id. Must not be <code>null</code>
	 * @return the value of the <code>id</code> field
	 */
	public static Object getId(Object object) {
		Objects.requireNonNull(object);
		try {
			Field idField = getIdField(object.getClass());
			if (idField == null) throw new IllegalArgumentException(object.getClass().getName() + " has no id field to get");
			Object id = idField.get(object);
			return id;
		} catch (SecurityException | IllegalAccessException e) {
			throw new LoggingRuntimeException(e, logger, "getting Id failed");
		}
	}
	
	/**
	 * @param modelClass
	 *            the model class for which an id should be parsed (used to
	 *            determine the class of the id - remember code classes could
	 *            have string id)
	 * @param idString
	 *            the id as string
	 * @return the id object usable for persistence
	 */
	public static Object parseId(Class<?> modelClass, String idString) {
		Field idField = getIdField(modelClass);
		if (idField == null) throw new IllegalArgumentException(modelClass.getName() + " has no id field");
		Class<?> idFieldType = idField.getType();
		if (idFieldType == Object.class || idFieldType == String.class) {
			return idString;
		} else if (idFieldType == Integer.class) {
			return Integer.parseInt(idString);
		} else if (idFieldType == Long.class) {
			return Long.parseLong(idString);
		} else {
			throw new IllegalArgumentException("Not a valid id class in " + modelClass.getSimpleName());
		}
	}
	
	/**
	 * @param modelClass
	 *            the model class for which an id should converted 
	 * @param value
	 *            the id
	 * @return the id object usable for persistence
	 */
	public static Object convertId(Class<?> modelClass, Object value) {
		if (value == null) {
			return null;
		}
		Field idField = getIdField(modelClass);
		if (idField == null) throw new IllegalArgumentException(modelClass.getName() + " has no id field");
		Class<?> idFieldType = idField.getType();
		if (idFieldType == Object.class || idFieldType.isAssignableFrom(value.getClass())) {
			return value;
		} else if (idFieldType == String.class) {
			return value.toString();
		} else if (idFieldType == Integer.class) {
			return ((Number) value).intValue();
		} else if (idFieldType == Long.class) {
			return ((Number) value).longValue();
		} else {
			throw new IllegalArgumentException("Not a valid id class in " + modelClass.getSimpleName());
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
	 * @param version the new value.
	 */
	public static void setVersion(Object object, int version) {
		try {
			Field versionField = object.getClass().getField("version");
			versionField.set(object, version);
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static int getVersion(Object object) {
		try {
			Field versionField = object.getClass().getField("version");
			return (Integer) versionField.get(object);
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void setHistorized(Object object, int historized) {
		try {
			Field historizedField = object.getClass().getField("historized");
			historizedField.set(object, historized > 0);
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

}
