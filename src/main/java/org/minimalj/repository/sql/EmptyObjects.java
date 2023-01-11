package org.minimalj.repository.sql;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.minimalj.model.Selection;
import org.minimalj.util.EqualsHelper;

@SuppressWarnings("unchecked")
public class EmptyObjects {
	private static Map<Class<?>, Object> emptyObjects = new HashMap<>();

	public static <T> boolean isEmpty(T object) {
		if (object == null || object.equals("")) {
			return true;
		} else if (object instanceof Collection) {
			return ((Collection<?>) object).isEmpty();
		} else if (object instanceof Integer) {
			return (Integer) object == 0;
		} else if (object instanceof Long) {
			return (Long) object == 0;
		} else if (object instanceof BigDecimal) {
			return ((BigDecimal) object).signum() == 0;
		} else if (object instanceof char[]) {
			return ((char[]) object).length == 0;
		} else if (object instanceof byte[]) {
			return ((byte[]) object).length == 0;
		} else if (object instanceof Selection) {
			return isEmpty(((Selection<?>) object).selectedValue);
		} else {
			Class<T> clazz = (Class<T>) object.getClass();
			if (clazz.getName().startsWith("java") || Enum.class.isAssignableFrom(clazz))
				return false;

			T emptyObject = getEmptyObject(clazz);
			return EqualsHelper.equals(emptyObject, object);
		}
	}

	public static <T> T getEmptyObject(Class<T> clazz) {
		if (!emptyObjects.containsKey(clazz)) {
			try {
				Object emptyObject = clazz.getConstructor().newInstance();
				emptyObjects.put(clazz, emptyObject);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return (T) emptyObjects.get(clazz);
	}

}
