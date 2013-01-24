package ch.openech.mj.db;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import ch.openech.mj.db.model.ColumnProperties;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.annotation.AnnotationUtil;
import ch.openech.mj.util.FieldUtils;

public class DbCreator {
	
	private static final Logger logger = Logger.getLogger(DbCreator.class.getName());
	
	private final DbPersistence dbPersistence;
	
	public DbCreator(DbPersistence dbPersistence) {
		this.dbPersistence = dbPersistence;
	}
	
	public void create(AbstractTable<?> table) throws SQLException {
		if (existTable(table)) return;
		
		Statement statement = dbPersistence.getConnection().createStatement();
		try {
			List<String> createStatements = getCreateStatements(table);
			for (String createStatement : createStatements) {
				logger.fine(createStatement);
				System.out.println(createStatement);
				statement.execute(createStatement);
			}
		} finally {
			statement.close();
		}
	}
	
	public boolean existTable(AbstractTable<?> table) throws SQLException {
		Statement statement = dbPersistence.getConnection().createStatement();
		try {
			statement.execute("select * from " + table.getTableName());
		} catch (SQLException x) {
			return false;
		} finally {
			statement.close();
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
			} else {
				addColumnDefinition(s, property);
			}
			
			s.append(ColumnProperties.isRequired(property) ? " NOT NULL" : " DEFAULT NULL");
			
//Eine Referenz auf eine einzelne Spalte, die nicht primary key ist
//scheint in Derby nicht möglich
//				
//				if (attributes.getNamedItem("referencedTable") != null) {
//					s.append(" CONSTRAINT con_"); s.append(tableName); s.append("_"); s.append(columnName);
//					String referencedTable = attributes.getNamedItem("referencedTable").getTextContent();
//					String referencedColumn = attributes.getNamedItem("referencedColumn").getTextContent();
//					// REFERENCES Cities ON DELETE CASCADE ON UPDATE RESTRICT
//					s.append(" REFERENCES "); s.append(referencedTable);
//					s.append(" ("); s.append(referencedColumn);
//					s.append(") ON DELETE CASCADE");
//				}
			s.append(",\n");
//			} else if ("foreign-key".equals(node.getNodeName())) {
//				appendConstraint(node, s);
//				s.append(",\n");
//			} else if ("index".equals(node.getNodeName())) {
//				createIndexStatements.add(createIndex(tableName, node));
		}
		
		if (table instanceof Table<?>) {
			s.append(" version INTEGER NOT NULL");
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id, version)\n");
			}
		} else if (table instanceof SubTable) {
			s.append(" startVersion INTEGER NOT NULL,\n");
			s.append(" endVersion INTEGER NOT NULL,\n");
			s.append(" position INTEGER NOT NULL");
			if (dbPersistence.isMySqlDb()) {
				s.append(",\n PRIMARY KEY (id, startVersion, position)\n");
			}
		} else {
			if (dbPersistence.isMySqlDb()) {
				s.append(" PRIMARY KEY (id)\n");
			} else {
				s.delete(s.length()-2, s.length()-1);
			}
		}
		
		s.append(")");
		appendTableEnd(s);
		s.append("\n");
		createStatements.add(s.toString());
		
		return createStatements;
	}
	
//	private static String createIndex(String tableName, Node index) {
//		StringBuilder s = new StringBuilder();
//		String indexName = index.getAttributes().getNamedItem("name").getTextContent();
//		s.append("CREATE INDEX "); s.append(indexName); s.append(" ON "); s.append(tableName); s.append(" (");
//		
//		List<String> columnNames = new ArrayList<String>();
//		NodeList nodeList = index.getChildNodes();
//		for (int j = 0; j<nodeList.getLength(); j++) {
//			Node node = nodeList.item(j);
//			if ("index-column".equals(node.getNodeName())) {
//				columnNames.add(node.getAttributes().getNamedItem("name").getTextContent());
//			}
//		}
//		appendStringList(columnNames, s);
//		s.append(")");
//		return s.toString();
//	}
	
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
			// TODO MySql enum?S
			s.append("INTEGER");
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
	
	
//	private static void appendConstraint(Node constraintNode, StringBuilder s) {
//		List<String> localColumns = new ArrayList<String>();
//		List<String> foreignColumns = new ArrayList<String>();
//		NodeList nodeList = constraintNode.getChildNodes();
//		for (int j = 0; j<nodeList.getLength(); j++) {
//			Node node = nodeList.item(j);
//			if ("reference".equals(node.getNodeName())) {
//				localColumns.add(node.getAttributes().getNamedItem("local").getTextContent());
//				foreignColumns.add(node.getAttributes().getNamedItem("foreign").getTextContent());
//			}
//		}
//		s.append("CONSTRAINT "); s.append(constraintNode.getAttributes().getNamedItem("name").getTextContent());
//		s.append(" FOREIGN KEY (");
//		appendStringList(localColumns, s);
//		s.append(") REFERENCES "); s.append(constraintNode.getAttributes().getNamedItem("foreignTable").getTextContent());
//		s.append(" (");
//		appendStringList(foreignColumns, s);
//		s.append(")");
//	}
//	
//	private static void appendStringList(List<String> strings, StringBuilder s) {
//		for (int i = 0; i<strings.size(); i++) {
//			s.append(strings.get(i));
//			if (i < strings.size() -1) s.append(", ");
//		}
//	}
}
