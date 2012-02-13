package ch.openech.mj.db;

import java.util.HashMap;
import java.util.Map;

import ch.openech.mj.db.model.ColumnAccess;

public class EmptyObjects {
	private static Map<Class<?>, Object> emptyObjects = new HashMap<Class<?>, Object>();

	public static <T> boolean isEmpty(T object) {
		T emptyObject = getEmptyObject((Class<T>) object.getClass());
		return ColumnAccess.equals(emptyObject, object);
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
