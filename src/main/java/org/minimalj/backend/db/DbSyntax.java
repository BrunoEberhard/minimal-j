package org.minimalj.backend.db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.IdUtils;

/**
 * The db product specifics
 * 
 */
public abstract class DbSyntax {

	public abstract int getMaxIdentifierLength();

	protected void addCreateStatementBegin(StringBuilder s, String tableName) {
		s.append("CREATE TABLE "); s.append(tableName); s.append(" (\n");
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
			s.append(")");
		} else if (idClass == Object.class) {
			s.append("CHAR(36)");
		} else {
			throw new IllegalArgumentException();
		}
		s.append(" NOT NULL");
	}
	
	/*
	 * Only public for tests. If this method doesnt throw an IllegalArgumentException
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
			s.append(" (").append(size).append(")");
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
				s.append(" (").append(size).append(")");
			} else {
				s.append(" (").append(size).append(", ").append(decimal).append(")");
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
				s.append(" (").append(size).append(")");
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
		s.append(")");
	}

	protected void addCreateStatementEnd(StringBuilder s) {
		s.append("\n)");
	}
	
	public String createConstraint(String tableName, String column, String referencedTableName, boolean referencedTableIsHistorized) {
		StringBuilder s = new StringBuilder();
		s.append("ALTER TABLE "); s.append(tableName);
		s.append(" ADD CONSTRAINT FK_");
		s.append(tableName); s.append("_"); s.append(column);
		s.append(" FOREIGN KEY (");
		s.append(column);
		s.append(") REFERENCES ");
		s.append(referencedTableName);
		s.append(" (ID)"); // not used at the moment: INITIALLY DEFERRED
		return s.toString();
	}
	
	public String createIndex(String tableName, String column, boolean withVersion) {
		StringBuilder s = new StringBuilder();
		s.append("CREATE INDEX IDX_");
		s.append(tableName);
		s.append('_');
		s.append(column);
		s.append(" ON ");
		s.append(tableName);
		s.append("(");
		s.append(column);
		if (withVersion) {
			s.append(", version");
		}
		s.append(")");
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
		s.append(")");
		return s.toString();
	}
	
	public static class MariaDbSyntax extends DbSyntax {
		
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
	}
	
	public static class DerbyDbSyntax extends DbSyntax {

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
		public void addPrimaryKey(StringBuilder s, String keys) {
			if (keys.indexOf(",") < 0) {
				super.addPrimaryKey(s, keys);
			} else {
				// no multi column primary keys possible
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
			s.append(")");
			return s.toString();
		}

		@Override
		public int getMaxIdentifierLength() {
			return 128;
		}
	}
	
}
