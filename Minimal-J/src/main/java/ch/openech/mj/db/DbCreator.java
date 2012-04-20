package ch.openech.mj.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.ColumnAccess;
import ch.openech.mj.db.model.Format;
import ch.openech.mj.db.model.Formats;
import ch.openech.mj.util.FieldUtils;

public class DbCreator {
	
	private final DbPersistence dbPersistence;
	
	public DbCreator(DbPersistence dbPersistence) {
		this.dbPersistence = dbPersistence;
	}
	
	public void createDb(AbstractTable<?> table) throws SQLException {
		if (existTable(table)) return;
		
		Statement statement = dbPersistence.getConnection().createStatement();
		List<String> createStatements = getCreateStatements(table);
		for (String createStatement : createStatements) {
			try {
				System.out.println(createStatement);
				statement.execute(createStatement);
			} catch (SQLException x) {
				System.out.println(x.getMessage());
			}
		}
		statement.close();
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
		
		Map<String, AccessorInterface> accessors = ColumnAccess.getAccessors(table.getClazz());
		for (String column : table.getColumnNames()) {
			AccessorInterface accessor = accessors.get(column);
			if (FieldUtils.isList(accessor.getClazz())) continue;
			
			s.append(" "); s.append(column);
			Format format = Formats.getInstance().getFormat(accessor);
			if (format != null) {
				String dbType = convertClassToActualDB(format.getClazz());
				s.append(" ");  s.append(dbType);
				appendSize(s, dbType, format.getSize());
			} else if (ColumnAccess.isReference(accessor)) {
				s.append(" INTEGER");
			} else {
				throw new IllegalArgumentException(column + " in Table: " + table.getTableName());
			}
			
			s.append(ColumnAccess.isRequired(accessor) ? " NOT NULL" : " DEFAULT NULL");
			
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
			if (dbPersistence.isMySqlDb()) {
				s.append(" version INTEGER NOT NULL,\n");
				s.append(" PRIMARY KEY (id, version)\n");
			} else {
				s.append(" version INTEGER NOT NULL");
			}
		} else if (table instanceof SubTable) {
			if (dbPersistence.isMySqlDb()) {
				s.append(" version INTEGER NOT NULL,\n");
				s.append(" endVersion INTEGER NOT NULL,\n");
				s.append(" position INTEGER NOT NULL,\n");
				s.append(" PRIMARY KEY (id, version, position)\n");
			} else {
				s.append(" version INTEGER NOT NULL,\n");
				s.append(" endVersion INTEGER NOT NULL,\n");
				s.append(" position INTEGER NOT NULL");
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
	
	private String convertClassToActualDB(Class<?> clazz) {
		if (clazz.equals(String.class) || clazz.equals(Date.class)) return "VARCHAR";
		if (clazz.equals(Integer.class)) return "INTEGER";
		if (clazz.equals(Boolean.class)) {
			if (dbPersistence.isDerbyDb()) {
				if (clazz.equals(Boolean.class)) return "SMALLINT";
			} else {
				if (clazz.equals(Boolean.class)) return "BIT";
			}
		}
		throw new IllegalArgumentException(clazz.toString());
//			if ("BIT".equals(accessorInterface) || "TINYINT".equals(accessorInterface)) accessorInterface = "SMALLINT";
//			if ("LONGVARCHAR".equals(accessorInterface)) accessorInterface = "CLOB";
//		} else if (dbPersistence.isMySqlDb()) {
//			if ("LONGVARCHAR".equals(accessorInterface)) accessorInterface = "TEXT";
//		}
//		return accessorInterface;
	}
	
	private void appendSize(StringBuilder s, String type, int size) {
		if (dbPersistence.isDerbyDb()) {
			// Integer and Timestamp have no size in Derby DB
			if (size != 0 && !type.contains("INT") && !type.equals("TIMESTAMP")) { 
				s.append("("); s.append(size); s.append(")"); 
			}
		} else if (dbPersistence.isMySqlDb()) {
			if (size != 0 && !type.equals("TIMESTAMP")) { 
				s.append("("); s.append(size); s.append(")");
			}
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
