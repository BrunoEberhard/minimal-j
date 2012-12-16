package ch.openech.mj.db.model;

import java.util.Map;
import java.util.WeakHashMap;

import org.joda.time.LocalDate;

public class InvalidValues {

	private static final Map<Object, String> values = new WeakHashMap<Object, String>();
	
	public static boolean isInvalid(Object key) {
		return values.containsKey(key);
	}
	
 	public static String getInvalidValue(Object key) {
		if (values.containsKey(key)) {
			return values.get(key);
		} else {
			throw new IllegalArgumentException("Key is not a illegal value: " + key);
		}
	}

	public static String createInvalidString(String string) {
		String s = new String(); // dont change
		values.put(s, string);
		return s;
	}
	
	public static Integer createInvalidInteger(String string) {
		Integer key = new Integer(Integer.MAX_VALUE);
		values.put(key, string);
		return key;
	}
	
	public static <T extends Enum<T>> Enum<T> createInvalidEnum(Class<T> enumClass, String value) {
		Enum<T> e = EnumUtils.createEnum(enumClass, "INVALID");
		values.put(e, value);
		return e;
	}

	public static LocalDate createInvalidLocalDate(String string) {
		LocalDate localDate = new LocalDate();
		values.put(localDate, string);
		return localDate;
	}

}
