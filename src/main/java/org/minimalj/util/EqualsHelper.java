package org.minimalj.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public class EqualsHelper {

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		if (from instanceof Collection) {
			Collection fromCollection = (Collection) from;
			Collection toCollection = (Collection) to;

			if (fromCollection.size() != toCollection.size()) return false;
			Iterator toIterator = toCollection.iterator();
			for (Object fromObject : fromCollection) {
				boolean itemEqual = equals(fromObject, toIterator.next());
				if (!itemEqual) return false;
			}
			return true;
		} 
		if (from.getClass() != to.getClass()) {
			return false;
		}
		if (IdUtils.hasId(from.getClass()) && !IdUtils.equals(from, to)) {
			return false;
		} 
		if (from instanceof Comparable && ((Comparable) from).compareTo(to) != 0) {
			return false;
		}
		try {
			return equalsByFields(from, to);
		} catch (IllegalAccessException | IllegalArgumentException x) {
			throw new RuntimeException(x);
		}
	}
	
	private static boolean equalsByFields(Object from, Object to) throws IllegalArgumentException, IllegalAccessException {
		for (Field field : from.getClass().getDeclaredFields()) {
			if (FieldUtils.isStatic(field)) continue;
			field.setAccessible(true);
			Object fromValue = field.get(from);
			Object toValue = field.get(to);
			
			boolean fieldEqual = equals(fromValue, toValue);
			if (!fieldEqual) {
				return false;
			}
		}
		return true;
	}
	
}
