package org.minimalj.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
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
		if (from.getClass() != to.getClass()) {
			return false;
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
		if (from.getClass().isArray()) {
			if (from instanceof byte[]) {
				return Arrays.equals((byte[]) from, (byte[]) to);
			} else if (from instanceof char[]) {
				return Arrays.equals((char[]) from, (char[]) to);
			} else {
				if (Array.getLength(from) != Array.getLength(to)) return false;
				for (int i = 0; i < Array.getLength(from); i++) {
					boolean itemEqual = equals(Array.get(from, i), Array.get(to, i));
					if (!itemEqual) return false;
				}
				return true;
			}
		}
		if (FieldUtils.isAllowedPrimitive(from.getClass()) || from instanceof UUID || from.getClass().isEnum()) {
			return ((Comparable) from).compareTo(to) == 0;
		}
		if (IdUtils.hasId(from.getClass()) && !IdUtils.equals(from, to)) {
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
