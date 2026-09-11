package org.minimalj.model.validation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.FieldProperty;
import org.minimalj.model.properties.Property;

/**
 * Invalid values represent strings that cannot be converted to objects of
 * their field classes.
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

	public static boolean isInvalid(Object object, Property property) {
		ObjectAndProperty objectAndProperty = new ObjectAndProperty(object, property);
		return isInvalid(objectAndProperty.object, objectAndProperty.field);
	}
	
	public static boolean isInvalid(Object object, Field field) {
		if (invalidStrings.containsKey(object)) {
			Map<Field, String> invalidFieldStringByField = invalidStrings.get(object);
			return invalidFieldStringByField.containsKey(field);
		} else {
			return false;
		}
	}

	public static String getInvalidString(Object object, Property property) {
		ObjectAndProperty objectAndProperty = new ObjectAndProperty(object, property);
		return getInvalidString(objectAndProperty.object, objectAndProperty.field);
	}

	public static String getInvalidString(Object object, Field field) {
		if (invalidStrings.containsKey(object)) {
			Map<Field, String> invalidFieldStringByField = invalidStrings.get(object);
			return invalidFieldStringByField.get(field);
		} else {
			return null;
		}
	}
	
	public static void setInvalidString(Object object, Property property, String string) {
		ObjectAndProperty objectAndProperty = new ObjectAndProperty(object, property);
		setInvalidString(objectAndProperty.object, objectAndProperty.field, string);
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

	public static void setValid(Object object, Property property) {
		ObjectAndProperty objectAndProperty = new ObjectAndProperty(object, property);
		setValid(objectAndProperty.object, objectAndProperty.field);
	}
	
	public static void setValid(Object object, Field field) {
		if (invalidStrings.containsKey(object)) {
			invalidStrings.get(object).remove(field);
		}
	}

	public static void clone(Object object, Object copy) {
		if (invalidStrings.containsKey(object)) {
			Map<Field, String> invalidFieldStringByField = invalidStrings.get(object);
			invalidStrings.put(copy, new HashMap<>(invalidFieldStringByField));
		}
	}
	
	private static class ObjectAndProperty {
		public final Object object;
		public final Field field;
		
		public ObjectAndProperty(Object object, Property property) {
			List<Property> chain = ChainedProperty.getChain(property);
			for (int i = 0; i < chain.size() - 1; i++) {
				object = chain.get(i).getValue(object);
			}
			this.object = object;
			Property lastProperty = chain.get(chain.size() - 1);
			if (lastProperty instanceof FieldProperty) {
				field = ((FieldProperty) lastProperty).getField();
			} else {
				field = null;
			}
		}
	}
	
}