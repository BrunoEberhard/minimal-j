package org.minimalj.repository.sql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.DateUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;

public abstract class SqlDialect {
	public static final Logger sqlLogger = Logger.getLogger("SQL");
	
	private final Set<String> foreignKeyNames = new HashSet<>();
	private final Set<String> indexNames = new HashSet<>();

	public abstract int getMaxIdentifierLength();

	protected void addCreateStatementBegin(StringBuilder s, String tableName) {
		s.append("CREATE TABLE ").append(tableName).append(" (\n");
	} 

	protected void addIdColumn(StringBuilder s, PropertyInterface idProperty) {
		Class<?> fieldClazz = idProperty.getClazz();
		int size = fieldClazz == String.class ? AnnotationUtil.getSize(idProperty) : 0;
		addIdColumn(s, fieldClazz, size);
	}
	
	protected void addIdColumn(StringBuilder s, Class<?> idClass, int size) {
		s.append(" id ");
		if (idClass == Integer.class) {
			s.append("INT");
		} else if (idClass == String.class) {
			s.append("VARCHAR(");
			s.append(size);
			s.append(')');
		} else if (idClass == Object.class) {
			s.append("CHAR(36)");
		} else {
			throw new IllegalArgumentException();
		}
		s.append(" NOT NULL");
	}
	
	/*
	 * Only public for tests. If this method doesn't throw an IllegalArgumentException
	 * then a property is valid
	 */
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
		} else if (clazz == LocalDate.class) {
			s.append("DATE");
		} else if (clazz == LocalTime.class) {
			s.append("TIME");		
		} else if (clazz == LocalDateTime.class) {
			s.append("DATETIME"); // MariaDB. DerbyDB is different
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
			s.append("BIT"); // MariaDB. DerbyDB is different
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
				PropertyInterface idProperty = Properties.getProperty(clazz, "id");
				addColumnDefinition(s, idProperty);
			} else {
				s.append("CHAR(36)");
			}
		}
	}
	
	protected void addPrimaryKey(StringBuilder s, String keys) {
		s.append(",\n PRIMARY KEY (");
		s.append(keys);
		s.append(')');
	}

	protected void addCreateStatementEnd(StringBuilder s) {
		s.append("\n)");
	}
	
	public String createConstraint(String tableName, String column, String referencedTableName, boolean referencedTableIsHistorized) {
		String name = "FK_" + tableName + "_" + column;
		name = SqlIdentifier.buildIdentifier(name, getMaxIdentifierLength(), foreignKeyNames);
		foreignKeyNames.add(name);
		
		StringBuilder s = new StringBuilder();
		s.append("ALTER TABLE ").append(tableName);
		s.append(" ADD CONSTRAINT ");
		s.append(name);
		s.append(" FOREIGN KEY (");
		s.append(column);
		s.append(") REFERENCES ");
		s.append(referencedTableName);
		s.append(" (ID)"); // not used at the moment: INITIALLY DEFERRED
		return s.toString();
	}
	
	public String createIndex(String tableName, String column, boolean withVersion) {
		String name = "IDX_" + tableName + "_" + column;
		name = SqlIdentifier.buildIdentifier(name, getMaxIdentifierLength(), indexNames);
		indexNames.add(name);
		
		StringBuilder s = new StringBuilder();
		s.append("CREATE INDEX ");
		s.append(name);
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
		StringBuilder s = new StringBuilder();
		s.append("ALTER TABLE ");
		s.append(tableName);
		s.append(" ADD UNIQUE INDEX ");
		s.append(column);
		s.append(" (");
		s.append(column);
		s.append(')');
		return s.toString();
	}
	
	public String limit(int rows, Integer offset) {
		return " OFFSET " + (offset != null ? offset.toString() : 0) + " ROWS FETCH NEXT " + rows + " ROWS ONLY";
	}
	
	public static class MariaSqlDialect extends SqlDialect {
		
		@Override
		protected void addCreateStatementEnd(StringBuilder s) {
			s.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED\n");
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
	
	public static class DerbySqlDialect extends SqlDialect {

		@Override
		public void addColumnDefinition(StringBuilder s, PropertyInterface property) {
			Class<?> clazz = property.getClazz();
			
			if (clazz == LocalDateTime.class) {
				s.append("TIMESTAMP");
			} else if (clazz == Boolean.class) {
				s.append("SMALLINT");
			} else {
				super.addColumnDefinition(s, property);
			}
		}
		
		
		@Override
		public String createConstraint(String tableName, String column, String referencedTableName, boolean referencedTableIsHistorized) {
			if (!referencedTableIsHistorized) {
				return super.createConstraint(tableName, column, referencedTableName, referencedTableIsHistorized);
			} else {
				return null;
			}
		}
		
		@Override
		public String createUniqueIndex(String tableName, String column) {
			StringBuilder s = new StringBuilder();
			s.append("ALTER TABLE ");
			s.append(tableName);
			s.append(" ADD CONSTRAINT ");
			s.append(column);
			s.append("_UNIQUE UNIQUE (");
			s.append(column);
			s.append(')');
			return s.toString();
		}

		@Override
		public int getMaxIdentifierLength() {
			return 128;
		}
	}

	public static class OracleSqlDialect extends SqlDialect {

		@Override
		public void setParameter(PreparedStatement preparedStatement, int param, Object value, PropertyInterface property) throws SQLException {
			if (value instanceof Temporal && !InvalidValues.isInvalid(value)) {
				value = value.toString();
			}
			super.setParameter(preparedStatement, param, value, property);
		}
		
		@Override
		public void setParameterNull(PreparedStatement preparedStatement, int param, PropertyInterface property) throws SQLException {
			Class<?> clazz = property.getClazz();
			if (clazz == LocalTime.class || clazz == LocalDate.class || clazz == LocalDateTime.class) {
				preparedStatement.setNull(param, Types.CHAR);
			} else {
				super.setParameterNull(preparedStatement, param, property);
			}
		}
		
		@Override
		public void addColumnDefinition(StringBuilder s, PropertyInterface property) {
			Class<?> clazz = property.getClazz();
			if (clazz == LocalDateTime.class) {
				s.append("TIMESTAMP");
			} else if (clazz == LocalDate.class) {
				s.append("CHAR(10)");
			} else if (clazz == LocalTime.class) {
				s.append("CHAR(").append(DateUtils.getTimeSize(property)).append(")");				
			} else if (clazz == LocalTime.class) {
				s.append("CHAR(30)");
			} else if (clazz == LocalDate.class) {
				s.append("DATE");
			} else if (clazz == Boolean.class) {
				s.append("SMALLINT");
			} else if (clazz == Long.class) {
				s.append("LONG");				
			} else {
				super.addColumnDefinition(s, property);
			}
		}
		
		@Override
		public String createConstraint(String tableName, String column, String referencedTableName, boolean referencedTableIsHistorized) {
			if (!referencedTableIsHistorized) {
				return super.createConstraint(tableName, column, referencedTableName, referencedTableIsHistorized);
			} else {
				return null;
			}
		}
		
		@Override
		public String createUniqueIndex(String tableName, String column) {
			StringBuilder s = new StringBuilder();
			s.append("ALTER TABLE ");
			s.append(tableName);
			s.append(" ADD CONSTRAINT ");
			s.append(column);
			s.append("_UNIQUE UNIQUE (");
			s.append(column);
			s.append(')');
			return s.toString();
		}

		@Override
		public int getMaxIdentifierLength() {
			return 30;
		}
	}
	
	public void setParameter(PreparedStatement preparedStatement, int param, Object value, PropertyInterface property) throws SQLException {
		if (value == null || InvalidValues.isInvalid(value)) {
			setParameterNull(preparedStatement, param, property);
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
		} else if (IdUtils.hasId(clazz)) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (property.getClazz().isArray()) {
			preparedStatement.setNull(param, Types.BLOB);			
		} else {
			preparedStatement.setNull(param, Types.INTEGER);
		}
	}
	
	protected Object convertToFieldClass(Class<?> fieldClass, Object value) {
		if (value == null) return null;
		
		if (fieldClass == LocalDate.class) {
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
