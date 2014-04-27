package ch.openech.mj.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.WeakHashMap;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Partial;

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

	public static Partial createInvalidPartial(String string) {
		Partial partial = new Partial();
		values.put(partial, string);
		return partial;
	}

	public static LocalDate createInvalidLocalDate(String string) {
		LocalDate localDate = new LocalDate();
		values.put(localDate, string);
		return localDate;
	}

	public static LocalDateTime createInvalidLocalDateTime(String string) {
		LocalDateTime localDateTime = new LocalDateTime();
		values.put(localDateTime, string);
		return localDateTime;
	}
	
	public static LocalTime createInvalidLocalTime(String string) {
		LocalTime localTime = new LocalTime();
		values.put(localTime, string);
		return localTime;
	}
	
	public static BigDecimal createInvalidBigDecimal(String string) {
		BigDecimal bigDecimal = new BigDecimal(0);
		values.put(bigDecimal, string);
		return bigDecimal;
	}
	
}
