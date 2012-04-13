package ch.openech.mj.edit.value;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.ColumnAccess;

public class CloneHelper {

	public static <T> T clone(T object) {
		if (object == null) return null;

		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) object.getClass();
		try {
			T copy = newInstance(clazz);
			deepCopy(object, copy);
			return copy;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deepCopy(Object from, Object to) {
		Map<String, AccessorInterface> accessors = ColumnAccess.getAccessors(from.getClass());
		for (AccessorInterface accessor : accessors.values()) {
			Object fromValue = accessor.getValue(from);
			if (accessor.isFinal()) {
				Object toValue = accessor.getValue(to);
				deepCopy(fromValue, toValue);
			} else if (fromValue != null && ColumnAccess.isReference(accessor)) {
				Object copyValue = CloneHelper.clone(fromValue);
				accessor.setValue(to, copyValue);
			} else {
				accessor.setValue(to, fromValue);
			}
		}
	}
	
	public static <T> T newInstance(Class<T> clazz) {
		try {
			Constructor<T> constructor = clazz.getConstructor();
			T newInstance = (T) constructor.newInstance();
			return newInstance;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
