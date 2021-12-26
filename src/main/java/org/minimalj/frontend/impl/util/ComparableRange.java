package org.minimalj.frontend.impl.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Predicate;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.FieldOperator;
import org.minimalj.util.DateUtils;
import org.minimalj.util.StringUtils;

public class ComparableRange implements Predicate<Object> {

	private final Comparable value1, value2;

	public ComparableRange(Class<?> clazz, String string) {
		value1 = parse(clazz, string, false);
		value2 = parse(clazz, string, true);
	}
	
	public ComparableRange(Class<?> clazz, String string1, String string2) {
		value1 = parse(clazz, string1, false);
		value2 = parse(clazz, string2, true);
	}

	public boolean valid() {
		if (value1 == null || InvalidValues.isInvalid(value1)) {
			return false;
		}
		if (value2 == null || InvalidValues.isInvalid(value2)) {
			return false;
		}
		return true;
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

	@Override
	public boolean test(Object value) {
		return compare((Comparable) value, value1) >= 0 && compare((Comparable) value, value2) <= 0;
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
	
	public Criteria getCriteria(PropertyInterface property) {
		if (valid()) {
			return By.field(property, FieldOperator.greaterOrEqual, value1).and(By.field(property, FieldOperator.lessOrEqual, value2));
		} else {
			return null;
		}
	}

}
