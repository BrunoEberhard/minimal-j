package org.minimalj.backend.db;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.derby.client.am.Types;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.InvalidValues;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.View;
import org.minimalj.model.annotation.ViewOf;
import org.minimalj.util.DateUtils;
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
	 * @return true if property isn't a simply object like String, Integer, Date etc but a immutable or reference
	 */
	public static boolean isReference(PropertyInterface property) {
		if (property.getFieldClazz().getName().startsWith("java")) return false;
		if (property.getFieldClazz().getName().startsWith("org.joda")) return false;
		if (Enum.class.isAssignableFrom(property.getFieldClazz())) return false;
		return true;
	}
	
	/**
	 * 
	 * @param property
	 * @return true if property or class of property is annotated as View
	 */
	public static boolean isView(Field field) {
		Class<?> clazz = field.getType();
		if (ViewOf.class.isAssignableFrom(clazz)) return true;
		if (field.getAnnotation(View.class) != null) return true;
		return false;
	}
	
	/**
	 * 
	 * @param property
	 * @return true if property or class of property is annotated as View
	 */
	public static boolean isView(PropertyInterface property) {
		Class<?> clazz = property.getFieldClazz();
		if (ViewOf.class.isAssignableFrom(clazz)) return true;
		if (property.getAnnotation(View.class) != null) return true;
		return false;
	}
	
	public static Class<?> getViewedClass(PropertyInterface property) {
		if (!isView(property)) throw new IllegalArgumentException(property.getFieldPath());
		View view = property.getAnnotation(View.class);
		if (view != null) return property.getFieldClazz();
		
		Class<?> clazz = property.getFieldClazz();
		return getViewedClass(clazz);
	}
	
	public static Class<?> getViewedClass(Class<?> clazz) {
		for (Type type : clazz.getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type rawType = parameterizedType.getRawType();
				if (rawType.equals(ViewOf.class)) {
					return GenericUtils.getGenericClass(parameterizedType);
				}
			}
		}
		return null;
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
				value = new java.sql.Date(((LocalDate) value).toDate().getTime());
			} else if (value instanceof LocalTime) {
				value = new java.sql.Time(((LocalTime) value).toDateTimeToday().getMillis());
			} else if (value instanceof LocalDateTime) {
				value = new java.sql.Timestamp(((LocalDateTime) value).toDate().getTime());
			} else if (value instanceof ReadablePartial) {
				value = DateUtils.formatPartial((ReadablePartial) value);
			} else if (value instanceof Set<?>) {
				Set<?> set = (Set<?>) value;
				Class<?> enumClass = GenericUtils.getGenericClass(property.getType());
				value = EnumUtils.getInt(set, enumClass);
			}
			preparedStatement.setObject(param, value);
		} 
	}

	public void setParameterNull(PreparedStatement preparedStatement, int param, PropertyInterface property) throws SQLException {
		Class<?> clazz = property.getFieldClazz();
		if (clazz == String.class) {
			preparedStatement.setNull(param, Types.VARCHAR);
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
		} else if (clazz == ReadablePartial.class) {
			preparedStatement.setNull(param, Types.CHAR);
		} else if (DbPersistenceHelper.isView(property)) {
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
				value = new LocalDate((java.sql.Date) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalTime.class) {
			if (value instanceof java.sql.Time) {
				value = new LocalTime((java.sql.Time) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalDateTime.class) {
			if (value instanceof java.sql.Timestamp) {
				value = new LocalDateTime((java.sql.Timestamp) value);
			} else if (value instanceof java.sql.Date) {
				value = new LocalDateTime((java.sql.Date) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == ReadablePartial.class) {
			if (value instanceof String) {
				String text = ((String) value).trim(); // cut the spaces from CHAR - Column
				value = DateUtils.parsePartial(text);
			} else if (value instanceof java.sql.Date) {
				// this should not happen, but is maybe usefull for migrating a DB
				value = new LocalDate(((java.sql.Date) value).getTime());
			} else if (value != null) {
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
		}
		return value;
	}

}
