package ch.openech.mj.db;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;

import ch.openech.mj.db.model.ColumnProperties;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.annotation.AnnotationUtil;
import ch.openech.mj.util.FieldUtils;

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
	
	public void create(AbstractTable<?> table) throws SQLException {
		if (existTable(table)) return;
		
		try (Statement statement = dbPersistence.getConnection().createStatement()) {
			List<String> createStatements = getCreateStatements(table);
			for (String createStatement : createStatements) {
				AbstractTable.sqlLogger.fine(createStatement);
				statement.execute(createStatement);
			}
		}
	}
	
	private boolean existTable(AbstractTable<?> table) throws SQLException {
		try (Statement statement = dbPersistence.getConnection().createStatement()) {
			String query = "select * from " + table.getTableName();
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
		
		Map<String, PropertyInterface> properties = ColumnProperties.getProperties(table.getClazz());
		for (String column : table.getColumnNames()) {
			PropertyInterface property = properties.get(column);
			if (FieldUtils.isList(property.getFieldClazz())) continue;
			
			s.append(" "); s.append(column); s.append(" "); 

			if (ColumnProperties.isReference(property)) {
				s.append("INTEGER");
				AbstractTable<?> referencedTable = dbPersistence.getTable(property.getFieldClazz());
				if (referencedTable instanceof ImmutableTable) {
					s.append(" REFERENCES "); s.append(referencedTable.getTableName());
				}
				// note: it's not possible to add a constraint to a versioned table
				// because of the start/endversion combinations
			} else {
				addColumnDefinition(s, property);
			}
			
			s.append(ColumnProperties.isRequired(property) ? " NOT NULL" : " DEFAULT NULL");
			s.append(",\n");
		}
		
		if (table instanceof HistorizedTable<?>) {
			s.append(" version INTEGER NOT NULL");
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id, version)\n");
			}
		} else if (table instanceof Table<?>) {
			s.delete(s.length()-2, s.length());
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id)\n");
			}
		} else if (table instanceof HistorizedSubTable) {
			s.append(" startVersion INTEGER NOT NULL,\n");
			s.append(" endVersion INTEGER NOT NULL,\n");
			s.append(" position INTEGER NOT NULL");
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id, startVersion, position)\n");
			}
		} else if (table instanceof SubTable) {
			s.append(" position INTEGER NOT NULL");
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id, position)\n");
			}
		} else {
			s.append(" PRIMARY KEY (id)\n");
		}
		
		s.append(")");
		appendTableEnd(s);
		s.append("\n");
		createStatements.add(s.toString());
		
		return createStatements;
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
		} else if (clazz.equals(BigDecimal.class)) {
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
			throw new IllegalArgumentException(property.getFieldName() +": " + clazz.toString());
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
