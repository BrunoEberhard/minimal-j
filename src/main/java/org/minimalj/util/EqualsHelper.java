package org.minimalj.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public class EqualsHelper {

	public static boolean equals(Object from, Object to) {
		if (from == null) {
			return to == null;
		}
		if (to == null) {
			return false;
		}
		if (FieldUtils.isAllowedPrimitive(from.getClass()) || from instanceof UUID) {
			return from.equals(to);
		}
		try {
			return _equals(from, to);
		} catch (IllegalAccessException | IllegalArgumentException x) {
			throw new RuntimeException(x);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static boolean _equals(Object from, Object to) throws IllegalArgumentException, IllegalAccessException {
		for (Field field : from.getClass().getDeclaredFields()) {
			if (FieldUtils.isStatic(field)) continue;
			field.setAccessible(true);
			Object fromValue = field.get(from);
			Object toValue = field.get(to);
			
			if (fromValue == null && toValue == null) continue;
			if (fromValue == null || toValue == null) return false;
			
			if (fromValue instanceof Collection) {
				Collection fromCollection = (Collection) fromValue;
				Collection toCollection = (Collection) toValue;

				if (fromCollection.size() != toCollection.size()) return false;
				Iterator toIterator = toCollection.iterator();
				for (Object fromObject : fromCollection) {
					boolean itemEqual = equals(fromObject, toIterator.next());
					if (!itemEqual) return false;
				}
			} else if (IdUtils.hasId(fromValue.getClass())) {
				if (!IdUtils.equals(fromValue, toValue)) {
					return false;
				}
			} else if (fromValue instanceof Comparable) {
				if (((Comparable) fromValue).compareTo(toValue) != 0) {
					return false;
				}
			} else {
				if (!fromValue.equals(toValue)) {
					return false;
				}
			}
		}
		return true;
	}
	
}
