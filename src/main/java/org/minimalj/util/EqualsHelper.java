package org.minimalj.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import org.minimalj.model.annotation.Reference;

public class EqualsHelper {

	public static boolean equals(Object from, Object to) {
		if (from == null) {
			return to == null;
		}
		if (to == null) {
			return false;
		}
		try {
			return _equals(from, to);
		} catch (IllegalAccessException | IllegalArgumentException x) {
			throw new RuntimeException(x);
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	private static boolean _equals(Object from, Object to) throws IllegalArgumentException, IllegalAccessException {
		for (Field field : from.getClass().getDeclaredFields()) {
			if (FieldUtils.isStatic(field)) continue;
			field.setAccessible(true);
			Object fromValue = field.get(from);
			Object toValue = field.get(to);
			
			if (fromValue == null && toValue == null) continue;
			if (fromValue == null && toValue != null) return false;
			if (fromValue != null && toValue == null) return false;
			
			if (fromValue instanceof Collection) {
				Collection fromCollection = (Collection) fromValue;
				Collection toCollection = (Collection) toValue;

				if (fromCollection.size() != toCollection.size()) return false;
				Iterator toIterator = toCollection.iterator();
				for (Object fromObject : fromCollection) {
					boolean itemEqual = _equals(fromObject, toIterator.next());
					if (!itemEqual) return false;
				}
			} else if (field.getAnnotation(Reference.class) != null || Codes.isCode(field.getType())) {
				return IdUtils.getId(fromValue).equals(IdUtils.getId(toValue));
			} else {
				if (!fromValue.equals(toValue)) {
					return false;
				}
			}
		}
		return true;
	}
	
}
