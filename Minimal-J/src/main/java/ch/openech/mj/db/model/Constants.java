package ch.openech.mj.db.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.util.FieldUtils;

public class Constants {

	private static List<ConstantName> constantNames = new ArrayList<ConstantName>();
	
	/**
	 * Warning: Should only be call once per class
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> T of (Class<T> clazz) {
		T object;
		try {
			object = clazz.newInstance();
			fillFields("", object);
			return object;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> void fillFields(String prefix, T object) throws IllegalAccessException, InstantiationException {
		fillFields(prefix, object, 0);
	}
	
	private static <T> void fillFields(String prefix, T object, int depth) throws IllegalAccessException, InstantiationException {
		Class<?> clazz = object.getClass();
		for (Field field : clazz.getFields()) {
			if (FieldUtils.isStatic(field)) continue;
			
			String fieldName = prefix + field.getName();
			if (field.getType().equals(String.class)) {
				// Strings are stored in the field itself
				field.set(object, fieldName);
				continue;
			} else {
				Object value = field.get(object);
				if (value == null) {
					// TODO doenst work, why?
					// if (FieldUtils.isAbstract(field) || FieldUtils.isInterface(field)) throw new InstantiationException(field.getName() + " with type " + field.getType() + " must be initialized at declaration");
					value = field.getType().newInstance();
					field.set(object, value);
				} 
				if (depth < 6) {
					fillFields(fieldName + ".", value, depth + 1);
				}
				constantNames.add(new ConstantName(value, fieldName));
			}
		}
	}
	
	public static String getConstant(Object object) {
		if (object instanceof String) {
			return (String) object;
		}
		for (ConstantName constantName : constantNames) {
			if (object == constantName.getObject()) {
				return constantName.getName();
			}
		}
		return null;
	}

	private static class ConstantName {
		
		private final Object object;
		private final String name;
		private ConstantName(Object object, String name) {
			this.object = object;
			this.name = name;
		}
		
		public Object getObject() {
			return object;
		}
		
		public String getName() {
			return name;
		}
	}

	public static String[] getConstants(Object[] fields) {
		String[] constants = new String[fields.length];
		for (int i = 0; i<fields.length; i++) {
			constants[i] = getConstant(fields[i]);
		}
		return constants;
	}
}
