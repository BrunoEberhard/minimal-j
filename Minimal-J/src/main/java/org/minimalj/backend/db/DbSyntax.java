package org.minimalj.backend.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.AnnotationUtil;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

/**
 * The db product specifics
 * 
 */
public abstract class DbSyntax {

	private boolean existTable(Connection connection, AbstractTable<?> table) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			String query = "select count(*) from " + table.getTableName();
			AbstractTable.sqlLogger.fine(query);
			statement.execute(query);
		} catch (SQLException x) {
			return false;
		} 
		return true;
	}

	protected void addCreateStatementBegin(StringBuilder s, String tableName) {
		s.append("CREATE TABLE "); s.append(tableName); s.append(" (\n");
	} 

	protected void addIdColumn(StringBuilder s) {
		s.append(" id BIGINT NOT NULL AUTO_INCREMENT");
	}
	
	/**
	 * Only public for tests. If this method doesnt throw an IllegalArgumentException
	 * then a property is valid
	 * 
	 * @param s
	 * @param property
	 */
	public void addColumnDefinition(StringBuilder s, PropertyInterface property) {
		Class<?> clazz = property.getFieldClazz();
		
		if (clazz == Integer.class) {
			s.append("INTEGER");
		} else if (clazz == Long.class) {
			s.append("BIGINT");
		} else if (clazz == String.class) {
			s.append("VARCHAR");
			int size = AnnotationUtil.getSize(property);
			s.append(" ("); s.append(size); s.append(")");
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
				s.append(" (" + size + ")");
			} else {
				s.append(" (" + size + ", " + decimal + ")");
			}
		} else if (clazz == Boolean.class) {
			s.append("BIT"); // MariaDB. DerbyDB is different
		} else if (Enum.class.isAssignableFrom(clazz)) {
			s.append("INTEGER");
		} else if (clazz == Set.class) {
			s.append("INTEGER");
		} else {
			throw new IllegalArgumentException(property.getDeclaringClass() + "." + property.getFieldName() +": " + clazz.toString());
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
	
	public String createConstraint(String tableName, String column, String referencedTableName) {
		StringBuilder s = new StringBuilder();
		s.append("ALTER TABLE "); s.append(tableName);
		s.append(" ADD CONSTRAINT FK_");
		s.append(tableName); s.append("_"); s.append(column);
		s.append(" FOREIGN KEY (");
		s.append(column);
		s.append(") REFERENCES ");
		s.append(referencedTableName);
		s.append(" (id)");
//		s.append(" ON DELETE CASCADE");
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
	
	public static class MariaDbSyntax extends DbSyntax {
		
		@Override
		protected void addCreateStatementEnd(StringBuilder s) {
			s.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED\n");
		}
	}
	
	public static class DerbyDbSyntax extends DbSyntax {

		@Override
		protected void addIdColumn(StringBuilder s) {
			s.append(" id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1)");
		}
		
		@Override
		public void addColumnDefinition(StringBuilder s, PropertyInterface property) {
			Class<?> clazz = property.getFieldClazz();
			
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
	}
	
}
