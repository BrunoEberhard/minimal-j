package org.minimalj.repository.sql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;
import org.minimalj.util.IdUtils;

public abstract class SqlDialect {
	public static final Logger sqlLogger = Logger.getLogger("SQL");
	
	public abstract int getMaxIdentifierLength();

	public boolean isValid(Connection connection) throws SQLException {
		return connection.isValid(0);
	}

	protected void addCreateStatementBegin(StringBuilder s, String tableName) {
		s.append("CREATE TABLE ").append(tableName).append(" (\n");
	} 

	protected void addIdColumn(StringBuilder s, PropertyInterface idProperty) {
		s.append(" id ");
		addColumnDefinition(s, idProperty);
		if (Table.isAutoIncrement(idProperty)) {
			s.append(" AUTO_INCREMENT");
		}
		s.append(" NOT NULL");
	}

	public void addColumnDefinition(StringBuilder s, PropertyInterface property) {
		Class<?> clazz = property.getClazz();
		
		if (clazz == Integer.class) {
			s.append("INTEGER");
		} else if (clazz == Long.class) {
			s.append("BIGINT");
		} else if (clazz == String.class) {
			s.append("VARCHAR");
			int size = AnnotationUtil.getSize(property);
			s.append(" (").append(size).append(')');
		} else if (clazz == Object.class) {
			if (!property.getName().equals("id")) {
				throw new IllegalArgumentException("Only id can be of class object. Field: " + property);
			}
			s.append("VARCHAR (36)");
		} else if (clazz == LocalDate.class) {
			s.append("DATE");
		} else if (clazz == LocalTime.class) {
			s.append("TIME");		
		} else if (clazz == LocalDateTime.class) {
			s.append("DATETIME");
		} else if (clazz == BigDecimal.class) {
			s.append("DECIMAL");
			int size = AnnotationUtil.getSize(property);
			int decimal = AnnotationUtil.getDecimal(property);
			if (decimal == 0) {
				s.append(" (").append(size).append(')');
			} else {
				s.append(" (").append(size).append(", ").append(decimal).append(')');
			}
		} else if (clazz == Boolean.class) {
			s.append("BIT");
		} else if (Enum.class.isAssignableFrom(clazz)) {
			s.append("INTEGER");
		} else if (clazz == Set.class) {
			s.append("INTEGER");
		} else if (clazz.isArray() && clazz.getComponentType() == Byte.TYPE) {
			s.append("BLOB");		
			int size = AnnotationUtil.getSize(property, AnnotationUtil.OPTIONAL);
			if (size > 0) {
				s.append(" (").append(size).append(')');
			}
		} else {
			if (IdUtils.hasId(clazz)) {
				PropertyInterface idProperty = FlatProperties.getProperty(clazz, "id");
				addColumnDefinition(s, idProperty);
			} else {
				PropertyInterface idProperty = FlatProperties.getProperty(property.getDeclaringClass(), "id");
				addColumnDefinition(s, idProperty);
			}
		}
	}
	
	public void addPrimaryKey(StringBuilder s, String keys) {
		s.append(",\n PRIMARY KEY (");
		s.append(keys);
		s.append(')');
	}

	protected void addCreateStatementEnd(StringBuilder s) {
		s.append("\n)");
	}

	public final String createConstraint(String constraintName, String tableName, String column, String referencedTableName) {
		// not used at the moment: INITIALLY DEFERRED
		return "ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName + " FOREIGN KEY (" + column + ") REFERENCES " + referencedTableName + " (ID)";
	}
	
	public String createIndex(String indexName, String tableName, String column, boolean withVersion) {
		StringBuilder s = new StringBuilder();
		s.append("CREATE INDEX ");
		s.append(indexName);
		s.append(" ON ");
		s.append(tableName);
		s.append('(');
		s.append(column);
		if (withVersion) {
			s.append(", version");
		}
		s.append(')');
		return s.toString();
	}
	
	public String createUniqueIndex(String tableName, String column) {
		return "ALTER TABLE " + tableName + " ADD UNIQUE INDEX " + column + " (" + column + ')';
	}
	
	public String limit(int rows, Integer offset) {
		return "OFFSET " + (offset != null ? offset.toString() : 0) + " ROWS FETCH NEXT " + rows + " ROWS ONLY";
	}
	
	public static class MariaSqlDialect extends SqlDialect {
		
		@Override
		protected void addCreateStatementEnd(StringBuilder s) {
			s.append("\n) CHARSET=utf8\n");
		}

		@Override
		public void addColumnDefinition(StringBuilder s, PropertyInterface property) {
			Class<?> clazz = property.getClazz();
			if (clazz.isArray() && clazz.getComponentType() == Byte.TYPE) {
				int size = AnnotationUtil.getSize(property, AnnotationUtil.OPTIONAL);
				if (size < 0) {
					s.append("LONGBLOB");	
				} else if (size < 256) {
					s.append("TINYBLOB");		
				} else if (size < 65536) {
					s.append("BLOB");		
				} else if (size < 16777215) {
					s.append("MEDIUMBLOB");		
				} else {
					s.append("LONGBLOB");		
				}
			} else  {
				super.addColumnDefinition(s, property);
			}
		}
		
		@Override
		public int getMaxIdentifierLength() {
			return 64;
		}
		
		@Override
		public String limit(int rows, Integer offset) {
			return "LIMIT " + rows + (offset != null ? " OFFSET " + offset.toString() : "");
		}
	}
	
	public static class PostgresqlDialect extends SqlDialect {
		
		@Override
		public boolean isValid(Connection connection) throws SQLException {
			// Postgres sometimes blocks when calling isValid() from different threads
			try {
				connection.createStatement().execute("select 1");
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		
		protected void addIdColumn(StringBuilder s, PropertyInterface idProperty) {
			Class<?> clazz = idProperty.getClazz();
			if (Table.isAutoIncrement(idProperty)) {
				if (clazz == Integer.class) {
					s.append(" id SERIAL");
				} else if (clazz == Long.class) {
					s.append(" id BIGSERIAL");
				} else {
					throw new IllegalArgumentException("Postgresql auto increment only possible for Integer and Long: " + idProperty);
				}
			} else {
				super.addIdColumn(s, idProperty);
			}
		}
		
		@Override
		protected void addCreateStatementEnd(StringBuilder s) {
			s.append("\n)");
		}

		@Override
		public void addColumnDefinition(StringBuilder s, PropertyInterface property) {
			Class<?> clazz = property.getClazz();
			if (clazz.isArray() && clazz.getComponentType() == Byte.TYPE) {
				s.append("BYTEA");	
			} else if (clazz == Boolean.class) {
				s.append("BOOLEAN");	
			} else if (clazz == LocalDateTime.class) {
				s.append("TIMESTAMP");		
			} else  {
				super.addColumnDefinition(s, property);
			}
		}
		
		@Override
		public void setParameter(PreparedStatement preparedStatement, int param, Object value) throws SQLException {
			if (value instanceof Boolean) {
				preparedStatement.setBoolean(param, (Boolean) value);
			} else {
				super.setParameter(preparedStatement, param, value);
			}
		}
		
		@Override
		public void setParameterNull(PreparedStatement preparedStatement, int param, Class<?> clazz) throws SQLException {
			if (clazz.isArray()) {
				preparedStatement.setNull(param, Types.ARRAY);			
			} else if (clazz == Boolean.class) {
				preparedStatement.setNull(param, Types.BOOLEAN);
			} else {
				super.setParameterNull(preparedStatement, param, clazz);
			}
		}
		
		@Override
		public int getMaxIdentifierLength() {
			return 63;
		}
		
		@Override
		public String limit(int rows, Integer offset) {
			return "LIMIT " + rows + (offset != null ? " OFFSET " + offset.toString() : "");
		}
	}
	
	public static class H2SqlDialect extends SqlDialect {
	
		@Override
		public int getMaxIdentifierLength() {
			// h2 doesn't really have a maximum identifier length
			return 256;
		}
	}
	
	public static class MsSqlDialect extends SqlDialect {

		@Override
		public void addColumnDefinition(StringBuilder s, PropertyInterface property) {
			Class<?> clazz = property.getClazz();
			if (clazz.isArray() && clazz.getComponentType() == Byte.TYPE) {
				int size = AnnotationUtil.getSize(property, AnnotationUtil.OPTIONAL);
				// https://docs.microsoft.com/en-us/sql/t-sql/data-types/binary-and-varbinary-transact-sql
				if (size > 0 && size <= 8000) {
					s.append("VARBINARY(" + size + ")");
				} else {
					s.append("VARBINARY(max)");
				}
			} else {
				super.addColumnDefinition(s, property);
			}
		}

		protected void addIdColumn(StringBuilder s, PropertyInterface idProperty) {
			s.append(" id ");
			addColumnDefinition(s, idProperty);
			if (Table.isAutoIncrement(idProperty)) {
				s.append(" IDENTITY(1,1)");
			}
			s.append(" NOT NULL");
		}

		@Override
		public int getMaxIdentifierLength() {
			return 128;
		}
	}

	public static class OracleSqlDialect extends SqlDialect {

		@Override
		public void setParameter(PreparedStatement preparedStatement, int param, Object value) throws SQLException {
			if (value instanceof Temporal && !InvalidValues.isInvalid(value)) {
				value = value.toString();
			}
			super.setParameter(preparedStatement, param, value);
		}
		
		@Override
		public void setParameterNull(PreparedStatement preparedStatement, int param, Class<?> clazz) throws SQLException {
			if (clazz == LocalTime.class || clazz == LocalDate.class || clazz == LocalDateTime.class) {
				preparedStatement.setNull(param, Types.CHAR);
			} else {
				super.setParameterNull(preparedStatement, param, clazz);
			}
		}
		
		@Override
		public void addColumnDefinition(StringBuilder s, PropertyInterface property) {
			Class<?> clazz = property.getClazz();
			if (clazz == LocalDate.class) {
				s.append("CHAR(10)");
			} else if (clazz == LocalTime.class) {
				s.append("CHAR(").append(DateUtils.getTimeSize(property)).append(")");				
			} else if (clazz == LocalDateTime.class) {
				s.append("CHAR(30)");
			} else if (clazz == Boolean.class) {
				s.append("SMALLINT");
			} else if (clazz == Long.class) {
				s.append("LONG");				
			} else {
				super.addColumnDefinition(s, property);
			}
		}
		
		@Override
		public String createUniqueIndex(String tableName, String column) {
			return "ALTER TABLE " + tableName + " ADD CONSTRAINT " + column + "_UNIQUE UNIQUE (" + column + ')';
		}

		@Override
		public int getMaxIdentifierLength() {
			return 30;
		}
	}
	
	public void setParameter(PreparedStatement preparedStatement, int param, Object value) throws SQLException {
		Objects.requireNonNull(value);
		if (InvalidValues.isInvalid(value)) {
			setParameterNull(preparedStatement, param, value.getClass());
		} else {
			if (value instanceof Enum<?>) {
				Enum<?> e = (Enum<?>) value;
				value = e.ordinal();
			} else if (value instanceof LocalDate) {
				value = java.sql.Date.valueOf((LocalDate) value);
			} else if (value instanceof LocalTime) {
				value = java.sql.Time.valueOf((LocalTime) value);
			} else if (value instanceof LocalDateTime) {
				value = java.sql.Timestamp.valueOf((LocalDateTime) value);
			} else if (value instanceof Set<?>) {
				value = EnumUtils.getInt((Set<?>) value);
			} else if (value instanceof UUID) {
				value = value.toString();
			}
			preparedStatement.setObject(param, value);
		} 
	}
	
	public void setParameterNull(PreparedStatement preparedStatement, int param, Class<?> clazz) throws SQLException {
		if (clazz == String.class) {
			preparedStatement.setNull(param, Types.VARCHAR);
		} else if (clazz == UUID.class) {
			preparedStatement.setNull(param, Types.CHAR);
		} else if (clazz == Integer.class) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == Boolean.class) {
			// TODO should this be BIT or BOOLEAN?
			// if change, adjust PostgresqlDialect and check for all dialects
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
		} else if (IdUtils.hasId(clazz)) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz.isArray()) {
			preparedStatement.setNull(param, Types.BLOB);			
		} else {
			preparedStatement.setNull(param, Types.INTEGER);
		}
	}
	
	protected Object convertToFieldClass(Class<?> fieldClass, Object value) {
		if (value == null) return null;
		
		if (fieldClass == Integer.class) {
			if (value instanceof Number) {
				value = ((Number) value).intValue();
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == Long.class) {
			if (value instanceof Number) {
				value = ((Number) value).longValue();
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalDate.class) {
			if (value instanceof java.sql.Date) {
				value = ((java.sql.Date) value).toLocalDate();
			} else if (value instanceof java.sql.Timestamp) {
				value = ((java.sql.Timestamp) value).toLocalDateTime().toLocalDate();
			} else if (value instanceof String) {
				value = LocalDate.parse((String) value);				
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalTime.class) {
			if (value instanceof java.sql.Time) {
				value = ((java.sql.Time) value).toLocalTime();
			} else if (value instanceof java.sql.Timestamp) {
				value = ((java.sql.Timestamp) value).toLocalDateTime().toLocalTime();
			} else if (value instanceof String) {
				value = LocalTime.parse((String) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalDateTime.class) {
			if (value instanceof java.sql.Timestamp) {
				value = ((java.sql.Timestamp) value).toLocalDateTime();
			} else if (value instanceof java.sql.Date) {
				value = ((java.sql.Date) value).toLocalDate().atStartOfDay();
			} else if (value instanceof java.sql.Timestamp) {
				value = ((java.sql.Timestamp) value).toLocalDateTime();
			} else if (value instanceof String) {
				value = LocalDateTime.parse((String) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == Boolean.class) {
			if (value instanceof Boolean) {
				return value;
			} else if (value instanceof Integer) {
				value = ((int) value) == 1;
			} else if (value != null) {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (Enum.class.isAssignableFrom(fieldClass)) {
			if (value instanceof Integer) {
				value = EnumUtils.valueList((Class<Enum>)fieldClass).get((Integer) value);
			} else if (value instanceof String) {
				value = Enum.valueOf((Class<Enum>)fieldClass, (String) value);
			} else if (value != null) {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == UUID.class) {
			value = UUID.fromString((String) value);
		}
		return value;
	}

}
