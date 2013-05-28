package ch.openech.mj.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Minimal-J internal<p>
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SubTable extends AbstractTable {

	private PreparedStatement selectByIdStatement;
	private PreparedStatement updateStatement;
	private PreparedStatement deleteStatement;
	
	public SubTable(DbPersistence dbPersistence, String prefix, Class clazz) {
		super(dbPersistence, prefix, clazz);
	}
	
	@Override
	public void initialize() throws SQLException {
		super.initialize();
		selectByIdStatement = prepare(selectByIdQuery());
		updateStatement = prepare(updateQuery());
		deleteStatement = prepare(deleteQuery());
	}
	
	@Override
	public void closeStatements() throws SQLException {
		super.closeStatements();
		selectByIdStatement.close();
		updateStatement.close();
		deleteStatement.close();
	}
	
	public void insert(int parentId, List objects) throws SQLException {
		for (int position = 0; position<objects.size(); position++) {
			Object object = objects.get(position);
			int parameterPos = setParameters(insertStatement, object, false, true);
			setParameterInt(insertStatement, parameterPos++, parentId);
			setParameterInt(insertStatement, parameterPos++, position);
			insertStatement.execute();
		}
	}

	public void update(int parentId, List objects) throws SQLException {
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
				int parameterPos = setParameters(insertStatement, objects.get(position), false, true);
				setParameterInt(insertStatement, parameterPos++, parentId);
				setParameterInt(insertStatement, parameterPos++, position);
				insertStatement.execute();
			}
			position++;
		}
	}

	private void update(int parentId, int position, Object object) throws SQLException {
		int parameterPos = setParameters(updateStatement, object, false, true);
		setParameterInt(updateStatement, parameterPos++, parentId);
		setParameterInt(updateStatement, parameterPos++, position);
		updateStatement.execute();
	}

	private void insert(int parentId, int position, Object object) throws SQLException {
		int parameterPos = setParameters(insertStatement, object, false, true);
		setParameterInt(insertStatement, parameterPos++, parentId);
		setParameterInt(insertStatement, parameterPos++, position);
		insertStatement.execute();
	}
	
	private void delete(int parentId, int position) throws SQLException {
		setParameterInt(deleteStatement, 0, parentId);
		setParameterInt(deleteStatement, 1, position);
		deleteStatement.execute();
	}

	public List read(int parentId) throws SQLException {
		selectByIdStatement.setInt(1, parentId);
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
		for (Object columnNameObject : columnNames) {
			// myst, direkt auf columnNames zugreiffen funktionert hier nicht
			String columnName = (String) columnNameObject;
			s.append(columnName);
			s.append(", ");
		}
		s.append("id, position) VALUES (");
		for (int i = 0; i<columnNames.size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?)");

		return s.toString();
	}

	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("UPDATE "); s.append(getTableName()); s.append(" SET ");
		for (Object columnNameObject : columnNames) {
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
}
