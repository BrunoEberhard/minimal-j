package org.minimalj.backend.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.Required;

/**
 * Minimal-J internal<p>
 * 
 * Sends the create statements to the DB.<p>
 * 
 * Normally you don't need this class directly. Add your classes
 * to the DbPersistence. By calling connect on the persisntence
 * this creator is used.
 * 
 */
public class DbCreator {
	private final DbPersistence dbPersistence;
	
	public DbCreator(DbPersistence dbPersistence) {
		this.dbPersistence = dbPersistence;
	}
	
	public void create(Connection connection, AbstractTable<?> table) throws SQLException {
		if (existTable(connection, table)) return;
		
		String lastStatement = null;
		try (Statement statement = connection.createStatement()) {
			List<String> createStatements = getCreateStatements(table);
			for (String createStatement : createStatements) {
				lastStatement = createStatement;
				AbstractTable.sqlLogger.fine(createStatement);
				statement.execute(createStatement);
			}
		} catch (SQLException x) {
			// in the SQLException the statement is not visible
			AbstractTable.sqlLogger.log(Level.SEVERE, lastStatement);
			throw x;
		}
	}
	
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

	public List<String> getCreateStatements(AbstractTable<?> table) throws SQLException {
		List<String> createStatements = new ArrayList<String>();
		
		StringBuilder s = new StringBuilder();
		s.append("CREATE TABLE "); s.append(table.getTableName()); s.append(" (\n");
		appendIdColumn(s);
		
		for (Map.Entry<String, PropertyInterface> column : table.getColumns().entrySet()) {
			PropertyInterface property = column.getValue();
			boolean isRequired = property.getAnnotation(Required.class) != null;
			
			s.append(" "); s.append(column.getKey()); s.append(" "); 

			if (DbPersistenceHelper.isReference(property) || DbPersistenceHelper.isView(property)) {
				s.append("INTEGER");
			} else {
				addColumnDefinition(s, property);
				s.append(isRequired ? " NOT NULL" : " DEFAULT NULL");
			}
			
			s.append(",\n");
		}
		
		if (table instanceof HistorizedTable<?>) {
			s.append(" version INTEGER NOT NULL");
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id, version)");
			}
		} else if (table instanceof Table<?>) {
			s.delete(s.length()-2, s.length());
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id)");
			}
		} else if (table instanceof HistorizedSubTable) {
			s.append(" startVersion INTEGER NOT NULL,\n");
			s.append(" endVersion INTEGER NOT NULL,\n");
			s.append(" position INTEGER NOT NULL");
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id, startVersion, position)");
			}
		} else if (table instanceof SubTable) {
			s.append(" position INTEGER NOT NULL");
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id, position)");
			}
		} else if (table instanceof ImmutableTable) {
			s.append(" hash INTEGER NOT NULL,\n");
			s.append(" PRIMARY KEY (id)");
		} else {
			s.append(" PRIMARY KEY (id)");
		}
		
		if (dbPersistence.isMySqlDb()) {
			appendIndexes(s, table);
			// appendConstraints(s, table);
		}
		s.append("\n)");
		appendTableEnd(s);
		s.append("\n");
		createStatements.add(s.toString());
		
		if (dbPersistence.isDerbyDb()) {
			createIndexStatements(createStatements, table);
		}
		
		return createStatements;
	}
	
	private void appendIndexes(StringBuilder s, AbstractTable<?> table) {
		Set<String> indexed = new TreeSet<>();
		for (String column : table.getIndexes()) {
			if (indexed.contains(column)) continue;
			indexed.add(column);
			
			s.append(",\n INDEX IDX_");
			s.append(table.getTableName());
			s.append('_');
			s.append(column);
			s.append(" (");
			s.append(column);
			if (table instanceof HistorizedTable) {
				s.append(", version");
			}
			s.append(")");
		}
		if (table instanceof ImmutableTable) {
			s.append(",\n INDEX IDX_");
			s.append(table.getTableName());
			s.append("_hash (hash)");
		}
	}

	private void appendConstraints(StringBuilder s, AbstractTable<?> table) {
		for (Map.Entry<String, PropertyInterface> column : table.getColumns().entrySet()) {
			PropertyInterface property = column.getValue();
			
			if (DbPersistenceHelper.isReference(property)) {
				Class<?> fieldClass = DbPersistenceHelper.isView(property) ? DbPersistenceHelper.getViewedClass(property) : property.getFieldClazz();
				AbstractTable<?> referencedTable = dbPersistence.table(fieldClass);

				s.append(",\n CONSTRAINT `FK_");
				s.append(table.getTableName()); s.append("_"); s.append(column.getKey());
				s.append("` FOREIGN KEY (`");
				s.append(column.getKey());
				s.append("`) REFERENCES `");
				s.append(referencedTable.getTableName());
				s.append("`");
				if (dbPersistence.isMySqlDb()) {
					s.append(" (id)");
				}				
				s.append(" ON DELETE CASCADE");
			}
		}
	}

	// 		CONSTRAINT `FK_person_event` FOREIGN KEY (`EVENT`) REFERENCES `event` (`id`),

	private void createIndexStatements(List<String> createStatements, AbstractTable<?> table) {
		Set<String> indexed = new TreeSet<>();
		for (String column : table.getIndexes()) {
			if (indexed.contains(column)) continue;
			indexed.add(column);
			
			StringBuilder s = new StringBuilder();
			s.append("CREATE INDEX IDX_");
			s.append(table.getTableName());
			s.append('_');
			s.append(column);
			s.append(" ON ");
			s.append(table.getTableName());
			s.append("(");
			s.append(column);
			if (table instanceof HistorizedTable) {
				s.append(", version");
			}
			s.append(")");
			createStatements.add(s.toString());
		}
		if (table instanceof ImmutableTable) {
			StringBuilder s = new StringBuilder();
			s.append("CREATE INDEX IDX_");
			s.append(table.getTableName());
			s.append("_hash ON ");
			s.append(table.getTableName());
			s.append("(hash)");
			createStatements.add(s.toString());
		}
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
		
		if (clazz.equals(Integer.class)) {
			s.append("INTEGER");
		} else if (clazz.equals(String.class)) {
			s.append("VARCHAR");
			int size = AnnotationUtil.getSize(property);
			s.append(" ("); s.append(size); s.append(")");
		} else if (clazz.equals(LocalDate.class)) {
			// TODO check partial
			s.append("DATE");
		} else if (clazz.equals(LocalTime.class)) {
			// TODO check partial
			s.append("TIME");		
		} else if (clazz.equals(LocalDateTime.class)) {
			if (dbPersistence.isDerbyDb()) {
				s.append("TIMESTAMP");
			} else {
				// MySQL
				s.append("DATETIME");
			}
		} else if (clazz.equals(BigDecimal.class) || clazz.equals(Long.class)) {
			s.append("DECIMAL");
			int size = AnnotationUtil.getSize(property);
			int decimal = AnnotationUtil.getDecimal(property);
			if (decimal == 0) {
				s.append(" (" + size + ")");
			} else {
				s.append(" (" + size + ", " + decimal + ")");
			}
		} else if (clazz.equals(Boolean.class)) {
			if (dbPersistence.isDerbyDb()) {
				s.append("SMALLINT");
			} else {
				s.append("BIT");
			}
		} else if (Enum.class.isAssignableFrom(clazz)) {
			s.append("INTEGER");
		} else if (clazz.equals(Set.class)) {
			s.append("INTEGER");
		} else if (clazz.equals(ReadablePartial.class)) {
			s.append("CHAR (10)");
		} else {
			throw new IllegalArgumentException(property.getDeclaringClass() + "." + property.getFieldName() +": " + clazz.toString());
		}
	}
	
	private void appendIdColumn(StringBuilder s) {
		if (dbPersistence.isDerbyDb()) {
			s.append(" id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),\n");
		} else if (dbPersistence.isMySqlDb()) {
			s.append(" id INT NOT NULL AUTO_INCREMENT,\n");
		}
	}

	private void appendTableEnd(StringBuilder s) {
		if (dbPersistence.isDerbyDb()) {
			// nothing
		} else if (dbPersistence.isMySqlDb()) {
			s.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED");
		}
	}
	
}
