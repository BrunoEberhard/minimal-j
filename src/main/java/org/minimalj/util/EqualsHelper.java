package org.minimalj.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EqualsHelper {

	public static boolean equals(Object from, Object to) {
		return equals(from, to, false, new HashMap<>());
	}

	public static boolean equalsById(Object from, Object to) {
		return equals(from, to, true, new HashMap<>());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static boolean equals(Object from, Object to, boolean equalsById, Map<Object, List<Object>> checked) {
		if (from == null) {
			return to == null;
		}
		if (to == null)
			return false;
		if (from.getClass() != to.getClass())
			return false;

		if (from instanceof Collection) {
			Collection fromCollection = (Collection) from;
			Collection toCollection = (Collection) to;

			if (fromCollection.size() != toCollection.size())
				return false;
			Iterator toIterator = toCollection.iterator();
			for (Object fromObject : fromCollection) {
				boolean itemEqual = equals(fromObject, toIterator.next(), equalsById, checked);
				if (!itemEqual)
					return false;
			}
			return true;
		}
		if (from.getClass().isArray()) {
			if (from instanceof byte[]) {
				return Arrays.equals((byte[]) from, (byte[]) to);
			} else if (from instanceof char[]) {
				return Arrays.equals((char[]) from, (char[]) to);
			} else {
				if (Array.getLength(from) != Array.getLength(to))
					return false;
				for (int i = 0; i < Array.getLength(from); i++) {
					boolean itemEqual = equals(Array.get(from, i), Array.get(to, i), equalsById, checked);
					if (!itemEqual)
						return false;
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
		if (from instanceof Map) {
			Map<?, ?> fromMap = (Map) from;
			Map<?, ?> toMap = (Map) to;
			if (fromMap.size() != toMap.size()) {
				return false;
			}
			for (Map.Entry<?, ?> entry: fromMap.entrySet()) {
				if (!equals(entry.getValue(), toMap.get(entry.getKey()))) {
					return false;
				}
			}
			return true;
		}
		if (!equalsById) {
			try {
				return equalsByFields(from, to, checked);
			} catch (IllegalAccessException | IllegalArgumentException x) {
				throw new RuntimeException(x);
			}
		} else {
			return true;
		}
	}

	private static boolean equalsByFields(Object from, Object to, Map<Object, List<Object>> checked) throws IllegalArgumentException, IllegalAccessException {
		if (checked.containsKey(from) && checked.get(from).contains(to)) {
			return true;
		} else {
			checked.computeIfAbsent(from, f -> new ArrayList<>()).add(to);
		}

		Class<?> clazz = from.getClass();
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (FieldUtils.isStatic(field))
					continue;
				field.setAccessible(true);
				Object fromValue = field.get(from);
				Object toValue = field.get(to);

				boolean fieldEqual = equals(fromValue, toValue, false, checked);
				if (!fieldEqual)
					return false;
			}
			clazz = clazz.getSuperclass();
		}
		return true;
	}

}
