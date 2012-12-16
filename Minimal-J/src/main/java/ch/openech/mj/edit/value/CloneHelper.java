package ch.openech.mj.edit.value;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.db.model.ColumnProperties;
import ch.openech.mj.db.model.ListColumnAccess;

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
		deepCopyListFields(from, to);
		deepCopyNonListFields(from, to);
	}

	private static void deepCopyNonListFields(Object from, Object to) {
		List<String> keys = ColumnProperties.getNonListKeys(from.getClass());
		for (String key: keys) {
			PropertyInterface property = ColumnProperties.getProperties(from.getClass()).get(key);
			Object fromValue = property.getValue(from);
			if (property.isFinal()) {
				Object toValue = property.getValue(to);
				deepCopy(fromValue, toValue);
			} else if (fromValue != null && ColumnProperties.isReference(property)) {
				Object copyValue = CloneHelper.clone(fromValue);
				property.setValue(to, copyValue);
			} else {
				property.setValue(to, fromValue);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void deepCopyListFields(Object from, Object to) {
		Map<String, PropertyInterface> listProperties = ListColumnAccess.getProperties(from.getClass());
		for (PropertyInterface property : listProperties.values()) {
			List fromValue = (List)property.getValue(from);
			if (fromValue == null) continue;
			List toValue = (List)property.getValue(to);
			if (!property.isFinal()) {
				toValue = new ArrayList();
				property.setValue(to, toValue);
			}
			for (Object element : fromValue) {
				toValue.add(clone(element));
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
