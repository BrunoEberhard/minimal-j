package org.minimalj.backend.db;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.GenericUtils;

public class DbPersistenceHelper {
	public static final Logger sqlLogger = Logger.getLogger("SQL");

	private final DbPersistence dbPersistence;
	
	public DbPersistenceHelper(DbPersistence dbPersistence) {
		this.dbPersistence = dbPersistence;
	}
	
	/**
	 * 
	 * @param property
	 * @return true if property isn't a simply object like String, Integer, Date etc but a dependable
	 */
	public static boolean isDependable(PropertyInterface property) {
		if (property.getClazz().getName().startsWith("java")) return false;
		if (Enum.class.isAssignableFrom(property.getClazz())) return false;
		if (property.isFinal()) return false;
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
				value = java.sql.Date.valueOf((LocalDate) value);
			} else if (value instanceof LocalTime) {
				value = java.sql.Time.valueOf((LocalTime) value);
			} else if (value instanceof LocalDateTime) {
				value = java.sql.Timestamp.valueOf((LocalDateTime) value);
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
	
	public void setParameterNull(PreparedStatement preparedStatement, int param, PropertyInterface property) throws SQLException {
		Class<?> clazz = property.getClazz();
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
		} else if (ViewUtil.isReference(property)) {
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
				value = ((java.sql.Date) value).toLocalDate();
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalTime.class) {
			if (value instanceof java.sql.Time) {
				value = ((java.sql.Time) value).toLocalTime();
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalDateTime.class) {
			if (value instanceof java.sql.Timestamp) {
				value = ((java.sql.Timestamp) value).toLocalDateTime();
			} else if (value instanceof java.sql.Date) {
				value = ((java.sql.Date) value).toLocalDate().atStartOfDay();
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == Boolean.class) {
			if (value instanceof Boolean) {
				return value;
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
	
	private static final List<String> SQL_KEYWORDS = Arrays.asList("SELECT", "FROM", "TO", "WHERE");
	
	public static String columnName(String name) {
		if (SQL_KEYWORDS.contains(name)) {
			return "'" + name + "'";
		} else {
			return name;
		}
	}

}
