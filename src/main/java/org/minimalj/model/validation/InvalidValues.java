package org.minimalj.model.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.minimalj.model.EnumUtils;

/**
 * Invalid values represent strings that cannot be converted in valid objects
 * as defined by their field classes.<p>
 * 
 * For example '42A' cannot be converted to an Integer. But temporarly it should
 * be stored in an Integer field because it's entered by the user.<p>
 * 
 * The form class converts the String in an InvalidValues object. So the
 * value can be stored in the Integer field. All validators should check for
 * invalid values when validating. And when a form field displays a value
 * it should check with the 'isInvalid' method if the value represents an
 * invalid value.
 *
 */
public class InvalidValues {

	private static final Map<Object, String> values = new WeakHashMap<Object, String>();
	private static int counter = Integer.MIN_VALUE + 1;
	private static LocalDate MIN_DATE = LocalDate.of(0, 1, 1);
	private static LocalTime MIN_TIME = LocalTime.of(0, 0, 0);
	private static LocalDateTime MIN_DATETIME = LocalDateTime.of(0, 1, 1, 0, 0, 0);
	
	/**
	 * @param value to be checked
	 * @return true if the value is an object created previously with one of the
	 * createInvalid methods
	 */
	public static boolean isInvalid(Object value) {
		return values.containsKey(value);
	}

	/**
	 * @param value a value with one of the createInvalid methods
	 * @return the string represented by this invalid value
	 * @throws IllegalArgumentException if the value wasn't created by one of the
	 * createInvalid methods
	 */
 	public static String getInvalidValue(Object value) {
		if (values.containsKey(value)) {
			return values.get(value);
		} else {
			throw new IllegalArgumentException("Key is not a illegal value: " + value);
		}
	}

	public static String createInvalidString(String string) {
		String s = new String("INVALID" + (counter++)); // dont change
		values.put(s, string);
		return s;
	}
	
	public static Integer createInvalidInteger(String string) {
		Integer key = new Integer(counter++);
		values.put(key, string);
		return key;
	}
	
	public static Long createInvalidLong(String string) {
		Long key = new Long(counter++);
		values.put(key, string);
		return key;
	}
	
	public static <T extends Enum<T>> T createInvalidEnum(Class<T> enumClass, String value) {
		T e = EnumUtils.createEnum(enumClass, "INVALID" + (counter++));
		values.put(e, value);
		return e;
	}

	public static Temporal createInvalidPartial(String string) {
		Temporal partial = MIN_DATE.plus(counter++, ChronoUnit.DAYS);
		values.put(partial, string);
		return partial;
	}

	public static LocalDate createInvalidLocalDate(String string) {
		LocalDate localDate = MIN_DATE.plus(counter++, ChronoUnit.DAYS);
		values.put(localDate, string);
		return localDate;
	}

	public static LocalDateTime createInvalidLocalDateTime(String string) {
		LocalDateTime localDateTime = MIN_DATETIME.plus(counter++, ChronoUnit.DAYS);
		values.put(localDateTime, string);
		return localDateTime;
	}
	
	public static LocalTime createInvalidLocalTime(String string) {
		LocalTime localTime = MIN_TIME.plus(counter++, ChronoUnit.MILLIS);
		values.put(localTime, string);
		return localTime;
	}
	
	public static BigDecimal createInvalidBigDecimal(String string) {
		BigDecimal bigDecimal = new BigDecimal(counter++);
		values.put(bigDecimal, string);
		return bigDecimal;
	}
	
	public static void markInvalid(Collection<?> object, String string) {
		values.put(object, string);
	}

	public static void markValid(Collection<?> object) {
		values.remove(object);
	}
}