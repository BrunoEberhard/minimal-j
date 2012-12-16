package ch.openech.mj.db;

import java.util.HashMap;
import java.util.Map;

import ch.openech.mj.db.model.ColumnProperties;

@SuppressWarnings("unchecked")
public class EmptyObjects {
	private static Map<Class<?>, Object> emptyObjects = new HashMap<Class<?>, Object>();

	public static <T> boolean isEmpty(T object) {
		if (object != null && !object.equals("")) {
			Class<T> clazz = (Class<T>) object.getClass();
			if (clazz.getName().startsWith("java") || clazz.getName().startsWith("org.joda") || //
					Enum.class.isAssignableFrom(clazz)) return false;
			
			T emptyObject = getEmptyObject(clazz);
			return ColumnProperties.equals(emptyObject, object);
		} else {
			return true;
		}
	}
	
	public static <T> T getEmptyObject(Class<T> clazz) {
		if (!emptyObjects.containsKey(clazz)) {
			try {
				Object emptyObject = clazz.newInstance();
				emptyObjects.put(clazz, emptyObject);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return (T) emptyObjects.get(clazz);
	}

}
