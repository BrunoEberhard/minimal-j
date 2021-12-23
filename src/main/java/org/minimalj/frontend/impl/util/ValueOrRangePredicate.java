package org.minimalj.frontend.impl.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.function.Predicate;

import org.minimalj.frontend.impl.util.ColumnFilter.ColumnFilterOperator;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;
import org.minimalj.util.StringUtils;

public class ValueOrRangePredicate implements Predicate<Object> {

	private final Class<?> clazz;

	private final ColumnFilterOperator operator;
	private final String string1, string2;
	private final Comparable value1, value2;

	public ValueOrRangePredicate(Class<?> clazz, String string) {
		this.clazz = clazz;
		
		if (!StringUtils.isEmpty(string)) {
			if (string.startsWith("-")) {
				string1 = string.substring(1).trim();
				string2 = null;
				value1 = parse(clazz, string1, true);
				value2 = null;
				operator = valid(value1) ? ColumnFilterOperator.MAX : null;
			} else if (string.endsWith("-")) {
				string1 = string.substring(0, string.length() - 1).trim();
				string2 = null;
				value1 = parse(clazz, string1, false);
				value2 = null;
				operator = valid(value1) ? ColumnFilterOperator.MIN : null;
			} else if (string.indexOf("-") > -1) {
				int pos = string.indexOf("-");
				string1 = string.substring(0, pos).trim();
				string2 = string.substring(pos + 1).trim();
				value1 = parse(clazz, string1, false);
				value2 = parse(clazz, string2, true);
				operator = valid(value1) && valid(value2) ? ColumnFilterOperator.RANGE : null;
			} else {
				string1 = string2 = string;
				if (Temporal.class.isAssignableFrom(clazz)) {
					value1 = parse(clazz, string, false);
					value2 = parse(clazz, string, true);
					operator = valid(value1) && valid(value2) ? ColumnFilterOperator.RANGE : null;
				} else {
					value1 = parse(clazz, string, null);
					value2 = null;
					operator = valid(value1) ? ColumnFilterOperator.EQUALS : null;
				}
			}
		} else {
			operator = null;
			string1 = string2 = null;
			value1 = value2 = null;
		}
	}
	
	private static boolean valid(Object value) {
		return value != null && !InvalidValues.isInvalid(value);
	}
	
	public static ValueOrRangePredicate create(Class<?> clazz, ColumnFilterOperator operator, String s1, String s2) {
		switch (operator) {
		case MIN:
			return new ValueOrRangePredicate(clazz, s1 + "-");
		case MAX:
			return new ValueOrRangePredicate(clazz, "-" + s1);
		case RANGE:
			return new ValueOrRangePredicate(clazz, s1 + "-" + s2);
		case EQUALS:
			return new ValueOrRangePredicate(clazz, s1);
		default:
			break;
		}
		return null;
	}
	
	public Comparable getValue1() {
		return value1;
	}
	
	public Comparable getValue2() {
		return value2;
	}
	
	public String getString1() {
		return string1;
	}
	
	public String getString2() {
		return string2;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
	
	public ColumnFilterOperator getOperator() {
		return operator;
	}
	
	public static Comparable parse(Class<?> clazz, String string, Boolean upperEnd) {
		if (!StringUtils.isEmpty(string)) {
			try {
				if (clazz == Integer.class) {
					return Integer.parseInt(string);
				} else if (clazz == Long.class) {
					return Long.parseLong(string);
				} else if (clazz == BigDecimal.class) {
					return new BigDecimal(string);
				} else if (clazz == LocalDate.class) {
					return DateUtils.parseDate(string, upperEnd);
				} else if (clazz == LocalTime.class) {
					return DateUtils.parseTime(string, upperEnd);
				} else if (clazz == LocalDateTime.class) {
					return DateUtils.parseDateTime(string, upperEnd);
				} else {
					throw new IllegalArgumentException(clazz.getName());
				}
			} catch (NumberFormatException ignored) {
				if (clazz == Integer.class) {
					return InvalidValues.createInvalidInteger(string);
				} else if (clazz == Long.class) {
					return InvalidValues.createInvalidLong(string);
				} else if (clazz == BigDecimal.class) {
					return InvalidValues.createInvalidBigDecimal(string);
				} else {
					throw new IllegalArgumentException(clazz.getName());
				}
			}
		}
		return null;
	}
	

	public boolean isRange() {
		return operator == ColumnFilterOperator.RANGE;
	}

	public boolean active() {
		if (value1 == null || InvalidValues.isInvalid(value1)) {
			return false;
		}
		if (isRange()) {
			if (value2 == null || InvalidValues.isInvalid(value2)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean test(Object value) {
		if (!active()) {
			return true;
		}
		
		Comparable v = (Comparable) value;
		if (v != null) {
			switch (operator) {
			case EQUALS:
				return compare(v, value1) == 0;
			case MAX:
				return compare(v, value1) <= 0;
			case MIN:
				return compare(v, value1) >= 0;
			case RANGE:
				if (value2 == null) {
					return true;
				}
				return compare(v, value1) >= 0 && compare(v, value2) <= 0;
			default:
				return true;
			}
		} else {
			return false;
		}
	}
	
	private int compare(Comparable c1, Comparable c2) {
		if (c1 instanceof LocalDateTime) {
			if (c2 instanceof LocalDateTime) {
				int result = ((LocalDateTime) c1).toLocalDate().compareTo(((LocalDateTime) c2).toLocalDate());
				if (result != 0) {
					return result;
				} else {
					c1 = ((LocalDateTime) c1).toLocalTime();
					c2 = ((LocalDateTime) c2).toLocalTime();
				}
			} else if (c2 instanceof LocalDate) {
				return ((LocalDateTime) c1).toLocalDate().compareTo((LocalDate) c2);
			}
		}
		return c1.compareTo(c2);
	}

}
