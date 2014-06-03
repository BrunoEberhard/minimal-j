package org.minimalj.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.WeakHashMap;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.Temporal;

public class InvalidValues {

	private static final Map<Object, String> values = new WeakHashMap<Object, String>();
	
	public static boolean isInvalid(Object value) {
		return values.containsKey(value);
	}
	
 	public static String getInvalidValue(Object value) {
		if (values.containsKey(value)) {
			return values.get(value);
		} else {
			throw new IllegalArgumentException("Key is not a illegal value: " + value);
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
	
	public static Long createInvalidLong(String string) {
		Long key = new Long(Long.MAX_VALUE);
		values.put(key, string);
		return key;
	}
	
	public static <T extends Enum<T>> T createInvalidEnum(Class<T> enumClass, String value) {
		T e = EnumUtils.createEnum(enumClass, "INVALID");
		values.put(e, value);
		return e;
	}

	public static Temporal createInvalidPartial(String string) {
		Temporal partial = LocalDate.now();
		values.put(partial, string);
		return partial;
	}

	public static LocalDate createInvalidLocalDate(String string) {
		LocalDate localDate = LocalDate.now();
		values.put(localDate, string);
		return localDate;
	}

	public static LocalDateTime createInvalidLocalDateTime(String string) {
		LocalDateTime localDateTime = LocalDateTime.now();
		values.put(localDateTime, string);
		return localDateTime;
	}
	
	public static LocalTime createInvalidLocalTime(String string) {
		LocalTime localTime = LocalTime.now();
		values.put(localTime, string);
		return localTime;
	}
	
	public static BigDecimal createInvalidBigDecimal(String string) {
		BigDecimal bigDecimal = new BigDecimal(0);
		values.put(bigDecimal, string);
		return bigDecimal;
	}
	
}
