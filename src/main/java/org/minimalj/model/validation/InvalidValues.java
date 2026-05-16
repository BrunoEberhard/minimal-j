package org.minimalj.model.validation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Invalid values represent strings that cannot be converted in valid objects as
 * defined by their field classes.
 * <p>
 * 
 * For example '42A' cannot be converted to an Integer. But temporarily it
 * should be stored in an Integer field because it's entered by the user or was
 * imported from an invalid source.
 * <p>
 * 
 * Invalid values <b>cannot</b> be persisted. They are only temporary objects in
 * the vm.
 * <p>
 *
 */
public class InvalidValues {

	private static final Map<Object, Map<Field, String>> invalidStrings = new WeakIdentityHashMap<>();

	public static String getInvalidString(Object object, Field field) {
		if (invalidStrings.containsKey(object)) {
			Map<Field, String> invalidFieldStringByField = invalidStrings.get(object);
			return invalidFieldStringByField.get(field);
		} else {
			return null;
		}
	}
	
	public static void setInvalidString(Object object, Field field, String string) {
		if (string != null) {
			invalidStrings.computeIfAbsent(object, o -> new HashMap<>()).put(field, string);
		} else {
			if (invalidStrings.containsKey(object)) {
				Map<Field, String> invalidFieldStringByField = invalidStrings.get(object);
				invalidFieldStringByField.remove(field);
				if (invalidFieldStringByField.isEmpty()) {
					invalidStrings.remove(object);
				}
			}			
		}
	}

	public static void clone(Object object, Object copy) {
		if (invalidStrings.containsKey(object)) {
			Map<Field, String> invalidFieldStringByField = invalidStrings.get(object);
			invalidStrings.put(copy, new HashMap<>(invalidFieldStringByField));
		}
	}
}