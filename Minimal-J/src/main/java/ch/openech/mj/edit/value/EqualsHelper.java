package ch.openech.mj.edit.value;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import ch.openech.mj.util.FieldUtils;

public class EqualsHelper {

	public static boolean equals(Object from, Object to) {
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
			
			if (FieldUtils.isList(field.getType()) || FieldUtils.isSet(field.getType())) {
				Collection fromList = (Collection)fromValue;
				Collection toList = (Collection)toValue;

				if (fromList.size() != toList.size()) return false;
				Iterator toIterator = toList.iterator();
				for (Object fromObject : fromList) {
					boolean itemEqual = _equals(fromObject, toIterator.next());
					if (!itemEqual) return false;
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
