package org.minimalj.backend.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Minimal-J internal<p>
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SubTable extends AbstractTable {

	private final String selectByIdQuery;
	private final String updateQuery;
	private final String deleteQuery;
	
	public SubTable(DbPersistence dbPersistence, String prefix, Class clazz) {
		super(dbPersistence, prefix, clazz);

		selectByIdQuery = selectByIdQuery();
		updateQuery = updateQuery();
		deleteQuery = deleteQuery();
	}
	
	public void insert(long parentId, List objects) throws SQLException {
		PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, false);
		for (int position = 0; position<objects.size(); position++) {
			Object object = objects.get(position);
			int parameterPos = setParameters(insertStatement, object, false, true);
			insertStatement.setLong(parameterPos++, parentId);
			insertStatement.setInt(parameterPos++, position);
			insertStatement.execute();
		}
	}

	public void update(long parentId, List objects) throws SQLException {
		List objectsInDb = read(parentId);
		int position = 0;
		while (position < Math.max(objects.size(), objectsInDb.size())) {
			boolean insert = false;
			if (position < objectsInDb.size() && position < objects.size()) {
				update(parentId, position, objects.get(position));
			} else if (position < objectsInDb.size()) {
				// delete all beginning from this position with one delete statement
				delete(parentId, position);
				break; 
			} else /* if (position < objects.size()) */ {
				insert(parentId, position, objects.get(position));
			}
			
			if (insert) {
				PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, false);
				int parameterPos = setParameters(insertStatement, objects.get(position), false, true);
				insertStatement.setLong(parameterPos++, parentId);
				insertStatement.setInt(parameterPos++, position);
				insertStatement.execute();
			}
			position++;
		}
	}

	private void update(long parentId, int position, Object object) throws SQLException {
		PreparedStatement updateStatement = getStatement(dbPersistence.getConnection(), updateQuery, false);

		int parameterPos = setParameters(updateStatement, object, false, true);
		updateStatement.setLong(parameterPos++, parentId);
		updateStatement.setInt(parameterPos++, position);
		updateStatement.execute();
	}

	private void insert(long parentId, int position, Object object) throws SQLException {
		PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, false);

		int parameterPos = setParameters(insertStatement, object, false, true);
		insertStatement.setLong(parameterPos++, parentId);
		insertStatement.setInt(parameterPos++, position);
		insertStatement.execute();
	}
	
	private void delete(long parentId, int position) throws SQLException {
		PreparedStatement deleteStatement = getStatement(dbPersistence.getConnection(), deleteQuery, false);

		deleteStatement.setLong(1, parentId);
		deleteStatement.setInt(2, position);
		deleteStatement.execute();
	}

	public List read(long parentId) throws SQLException {
		PreparedStatement selectByIdStatement = getStatement(dbPersistence.getConnection(), selectByIdQuery, false);

		selectByIdStatement.setLong(1, parentId);
		return executeSelectAll(selectByIdStatement);
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
	
	protected void addSpecialColumns(DbSyntax syntax, StringBuilder s) {
		super.addSpecialColumns(syntax, s);
		s.append(",\n position INTEGER NOT NULL");
	}
	
	protected void addPrimaryKey(DbSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id, position");
	}
}
