package org.minimalj.backend.db;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.InvalidValues;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.ViewUtil;
import org.minimalj.util.GenericUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

public class DbPersistenceHelper {
	public static final Logger sqlLogger = Logger.getLogger("SQL");

	private final DbPersistence dbPersistence;
	
	public DbPersistenceHelper(DbPersistence dbPersistence) {
		this.dbPersistence = dbPersistence;
	}
	
	/**
	 * 
	 * @param property
	 * @return true if property isn't a simply object like String, Integer, Date etc but a immutable or reference
	 */
	public static boolean isReference(PropertyInterface property) {
		if (property.getFieldClazz().getName().startsWith("java")) return false;
		if (property.getFieldClazz().getName().startsWith("org.threeten")) return false;
		if (Enum.class.isAssignableFrom(property.getFieldClazz())) return false;
		return true;
	}
	
	
	public void setParameter(PreparedStatement preparedStatement, int param, Object value, PropertyInterface property) throws SQLException {
		if (value == null) {
			setParameterNull(preparedStatement, param, property);
		} else {
			if (value instanceof Enum<?>) {
				Enum<?> e = (Enum<?>) value;
				if (!InvalidValues.isInvalid(e)) {
					value = e.ordinal();
				} else {
					setParameterNull(preparedStatement, param, property);
					return;
				}
			} else if (value instanceof LocalDate) {
				value = convertToSql((LocalDate) value);
			} else if (value instanceof LocalTime) {
				value = convertToSql((LocalTime) value);
			} else if (value instanceof LocalDateTime) {
				value = convertToSql((LocalDateTime) value);
			} else if (value instanceof Set<?>) {
				Set<?> set = (Set<?>) value;
				Class<?> enumClass = GenericUtils.getGenericClass(property.getType());
				value = EnumUtils.getInt(set, enumClass);
			} else if (value instanceof UUID) {
				value = value.toString();
			}
			preparedStatement.setObject(param, value);
		} 
	}

	// in jdk 8 there are converters. remove this when migrating
	
	static java.sql.Date convertToSql(LocalDate localDate) {
		@SuppressWarnings("deprecation")
		java.sql.Date timestamp = new java.sql.Date(localDate.getYear() - 1900, localDate.getMonthValue() - 1, localDate.getDayOfMonth());
		return timestamp;
	}
	
	static java.sql.Time convertToSql(LocalTime localTime) {
		@SuppressWarnings("deprecation")
		java.sql.Time time = new java.sql.Time(localTime.getHour(), localTime.getMinute(), localTime.getSecond());
		return time;
	}
	
	static java.sql.Timestamp convertToSql(LocalDateTime localDateTime) {
		@SuppressWarnings("deprecation")
		java.sql.Timestamp timestamp = new java.sql.Timestamp(localDateTime.getYear() - 1900, localDateTime.getMonthValue() - 1, localDateTime.getDayOfMonth(), //
				localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(), localDateTime.getNano());
		return timestamp;
	}
	
	@SuppressWarnings("deprecation")
	static LocalDate convertFromSql(java.sql.Date date) {
		return LocalDate.of(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
	}
	
	@SuppressWarnings("deprecation")
	static LocalTime convertFromSql(java.sql.Time time) {
		return LocalTime.of(time.getHours(), time.getMinutes(), time.getSeconds());
	}
	
	@SuppressWarnings("deprecation")
	static LocalDateTime convertFromSql(java.sql.Timestamp timestamp) {
		return LocalDateTime.of(timestamp.getYear() + 1900, timestamp.getMonth() + 1, timestamp.getDate(), timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds());
	}
	
	@SuppressWarnings("deprecation")
	static LocalDateTime convertFromSqlToLocalDateTime(java.sql.Date date) {
		return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds());
	}
	
	public void setParameterNull(PreparedStatement preparedStatement, int param, PropertyInterface property) throws SQLException {
		Class<?> clazz = property.getFieldClazz();
		if (clazz == String.class) {
			preparedStatement.setNull(param, Types.VARCHAR);
		} else if (clazz == UUID.class) {
			preparedStatement.setNull(param, Types.CHAR);
		} else if (clazz == Integer.class) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == Boolean.class) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == BigDecimal.class || clazz == Long.class) {
			preparedStatement.setNull(param, Types.DECIMAL);
		} else if (Enum.class.isAssignableFrom(clazz)) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == LocalDate.class) {
			preparedStatement.setNull(param, Types.DATE);
		} else if (clazz == LocalTime.class) {
			preparedStatement.setNull(param, Types.TIME);
		} else if (clazz == LocalDateTime.class) {
			preparedStatement.setNull(param, Types.DATE);
		} else if (ViewUtil.isView(property)) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (dbPersistence.table(clazz) != null) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else {
			throw new IllegalArgumentException(clazz.getSimpleName());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Object convertToFieldClass(Class<?> fieldClass, Object value) {
		if (value == null) return null;
		
		if (fieldClass == LocalDate.class) {
			if (value instanceof java.sql.Date) {
				value = convertFromSql((java.sql.Date) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalTime.class) {
			if (value instanceof java.sql.Time) {
				value = convertFromSql((java.sql.Time) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalDateTime.class) {
			if (value instanceof java.sql.Timestamp) {
				value = convertFromSql((java.sql.Timestamp) value);
			} else if (value instanceof java.sql.Date) {
				value = convertFromSqlToLocalDateTime((java.sql.Date) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == Boolean.class) {
			if (value instanceof Boolean) {
				return (Boolean) value;
			} else if (value instanceof Integer) {
				value = Boolean.valueOf(((int) value) == 1);
			} else if (value != null) {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (Enum.class.isAssignableFrom(fieldClass)) {
			value = EnumUtils.valueList((Class<Enum>)fieldClass).get((Integer) value);
		} else if (fieldClass == UUID.class) {
			value = UUID.fromString((String) value);
		}
		return value;
	}

}
