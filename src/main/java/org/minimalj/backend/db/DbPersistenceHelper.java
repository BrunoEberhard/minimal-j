package org.minimalj.backend.db;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.ReservedDbWords;

public class DbPersistenceHelper {
	public static final Logger sqlLogger = Logger.getLogger("SQL");

	private final DbPersistence dbPersistence;
	
	public DbPersistenceHelper(DbPersistence dbPersistence) {
		this.dbPersistence = dbPersistence;
	}
	
	/**
	 * @param property the property to check
	 * @return true if property isn't a base object like String, Integer, Date, enum but a dependable
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
	
	public static String buildName(String name, int maxLength, Set<String> alreadyUsedNames) {
		name = name.toUpperCase();
		name = cutToMaxLength(name, maxLength);
		name = avoidReservedDbWords(name, maxLength);
		name = resolveNameConflicts(alreadyUsedNames, name);
		return name;
	}
	
	private static String cutToMaxLength(String fieldName, int maxLength) {
		if (fieldName.length() > maxLength) {
			fieldName = fieldName.substring(0, maxLength);
		}
		return fieldName;
	}

	private static String avoidReservedDbWords(String fieldName, int maxLength) {
		if (ReservedDbWords.reservedDbWords.contains(fieldName)) {
			if (fieldName.length() == maxLength) {
				fieldName = fieldName.substring(0, fieldName.length() - 1);
			}
			fieldName = fieldName + "_";
		}
		return fieldName;
	}

	private static String resolveNameConflicts(Set<String> set, String fieldName) {
		if (set.contains(fieldName)) {
			int i = 1;
			do {
				String number = Integer.toString(i);
				String tryFieldName = fieldName.substring(0,  fieldName.length() - number.length() - 1) + "_" + number;
				if (!set.contains(tryFieldName)) {
					fieldName = tryFieldName;
					break;
				}
				i++;
			} while (true);
		}
		return fieldName;
	}
	
}
