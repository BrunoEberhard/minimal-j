package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.minimalj.model.properties.PropertyInterface;

/**
 * Minimal-J internal
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SubTable extends AbstractTable {

	private final String selectByIdQuery;
	private final String updateQuery;
	private final String deleteQuery;
	
	public SubTable(SqlPersistence sqlPersistence, String prefix, Class clazz, PropertyInterface idProperty) {
		super(sqlPersistence, prefix, clazz, idProperty);
		
		selectByIdQuery = selectByIdQuery();
		updateQuery = updateQuery();
		deleteQuery = deleteQuery();
	}
	
	public void insert(Object parentId, List objects) throws SQLException {
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			for (int position = 0; position<objects.size(); position++) {
				Object object = objects.get(position);
				int parameterPos = setParameters(insertStatement, object, false, ParameterMode.INSERT, parentId);
				insertStatement.setInt(parameterPos++, position);
				insertStatement.execute();
			}
		}
	}

	protected void update(Object parentId, List objects) throws SQLException {
		List objectsInDb = read(parentId);
		int position = 0;
		while (position < Math.max(objects.size(), objectsInDb.size())) {
			if (position < objectsInDb.size() && position < objects.size()) {
				update(parentId, position, objects.get(position));
			} else if (position < objectsInDb.size()) {
				// delete all beginning from this position with one delete statement
				delete(parentId, position);
				break; 
			} else /* if (position < objects.size()) */ {
				insert(parentId, position, objects.get(position));
			}
			position++;
		}
	}

	private void update(Object parentId, int position, Object object) throws SQLException {
		try (PreparedStatement updateStatement = createStatement(sqlPersistence.getConnection(), updateQuery, false)) {
			int parameterPos = setParameters(updateStatement, object, false, ParameterMode.UPDATE, parentId);
			updateStatement.setInt(parameterPos++, position);
			updateStatement.execute();
		}
	}

	private void insert(Object parentId, int position, Object object) throws SQLException {
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			int parameterPos = setParameters(insertStatement, object, false, ParameterMode.INSERT, parentId);
			insertStatement.setInt(parameterPos++, position);
			insertStatement.execute();
		}
	}
	
	private void delete(Object parentId, int position) throws SQLException {
		try (PreparedStatement deleteStatement = createStatement(sqlPersistence.getConnection(), deleteQuery, false)) {
			deleteStatement.setObject(1, parentId);
			deleteStatement.setInt(2, position);
			deleteStatement.execute();
		}
	}

	public List read(Object parentId) throws SQLException {
		try (PreparedStatement selectByIdStatement = createStatement(sqlPersistence.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, parentId);
			return executeSelectAll(selectByIdStatement);
		}
	}

	// Queries
	
	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); query.append(" WHERE id = ? ORDER BY position");
		return query.toString();
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO "); s.append(getTableName()); s.append(" (");
		for (Object columnNameObject : getColumns().keySet()) {
			// myst, direkt auf columnNames zugreiffen funktionert hier nicht
			String columnName = (String) columnNameObject;
			s.append(columnName);
			s.append(", ");
		}
		s.append("id, position) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?)");

		return s.toString();
	}

	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("UPDATE "); s.append(getTableName()); s.append(" SET ");
		for (Object columnNameObject : getColumns().keySet()) {
			s.append((String) columnNameObject);
			s.append("= ?, ");
		}
		s.delete(s.length()-2, s.length());
		s.append(" WHERE id = ? AND position = ?");

		return s.toString();
	}
	
	protected String deleteQuery() {
		return "DELETE FROM " + getTableName() + " WHERE id = ? AND position >= ?";
	}
	
	@Override
	protected void addSpecialColumns(SqlSyntax syntax, StringBuilder s) {
		s.append(" id ");
		syntax.addColumnDefinition(s, idProperty);
		s.append(",\n position INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id, position");
	}
}
