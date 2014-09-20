package org.minimalj.backend.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.minimalj.model.PropertyInterface;
import org.minimalj.util.EqualsHelper;

/**
 * Minimal-J internal<p>
 * 
 * HistorizedTable has one version columns, HistorizedSubTable hast two.
 * One for startVersion and one vor endVersion.<p>
 * 
 * For a new Entry there is startVersion and endVersion = 0.<p>
 * 
 * After an update endVersion contains the version from which the
 * entry is <i>not</i> valid anymore. The startVersion of the new
 * row contains the version from which the entry is valid.
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class HistorizedSubTable extends AbstractTable {

	private final String selectByIdAndTimeQuery;
	private final String endQuery;
	private final String readVersionsQuery;
	
	public HistorizedSubTable(DbPersistence dbPersistence, String prefix, Class clazz, PropertyInterface idProperty) {
		super(dbPersistence, prefix, clazz, idProperty);
		
		selectByIdAndTimeQuery = selectByIdAndTimeQuery();
		endQuery = endQuery();
		readVersionsQuery = readVersionsQuery();
	}
	
	public void insert(Object parentId, List objects, Integer version) throws SQLException {
		PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, false);
		for (int position = 0; position<objects.size(); position++) {
			Object object = objects.get(position);
			int parameterPos = setParameters(insertStatement, object, false, true, parentId);
			insertStatement.setInt(parameterPos++, position);
			insertStatement.setInt(parameterPos++, version);
			insertStatement.execute();
		}
	}

	public void update(Object parentId, List objects, int version) throws SQLException {
		List objectsInDb = read(parentId, version);
		int position = 0;
		PreparedStatement endStatement = getStatement(dbPersistence.getConnection(), endQuery, false);
		PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, false);
		while (position < Math.max(objects.size(), objectsInDb.size())) {
			boolean end = false;
			boolean insert = false;
			if (position < objectsInDb.size() && position < objects.size()) {
				Object object = objects.get(position);
				Object objectInDb = objectsInDb.get(position);
				end = insert = !EqualsHelper.equals(object, objectInDb);
			} else if (position < objectsInDb.size()) {
				end = true;
			} else /* if (position < objects.size()) */ {
				insert = true;
			}
			
			if (end) {
				endStatement.setInt(1, version);
				endStatement.setObject(2, parentId);
				endStatement.setInt(3, position);
				endStatement.execute();	
			}
			
			if (insert) {
				int parameterPos = setParameters(insertStatement, objects.get(position), false, true, parentId);
				insertStatement.setInt(parameterPos++, position);
				insertStatement.setInt(parameterPos++, version);
				insertStatement.execute();
			}
			position++;
		}
	}

	public List read(Object parentId, Integer time) throws SQLException {
		if (time == null) {
			return read(parentId);
		}
		PreparedStatement selectByIdAndTimeStatement = getStatement(dbPersistence.getConnection(), selectByIdAndTimeQuery, false);
		selectByIdAndTimeStatement.setObject(1, parentId);
		selectByIdAndTimeStatement.setInt(2, time);
		selectByIdAndTimeStatement.setInt(3, time);
		return executeSelectAll(selectByIdAndTimeStatement);
	}

	private List read(Object id) throws SQLException {
		PreparedStatement selectByIdStatement = getStatement(dbPersistence.getConnection(), selectByIdQuery, false);
		selectByIdStatement.setObject(1, id);
		return executeSelectAll(selectByIdStatement);
	}
	
	public void readVersions(Object parentId, List<Integer> result) throws SQLException {
		PreparedStatement readVersionsStatement = getStatement(dbPersistence.getConnection(), readVersionsQuery, false);
		readVersionsStatement.setObject(1, parentId);
		try (ResultSet resultSet = readVersionsStatement.executeQuery()) {
			while (resultSet.next()) {
				int version = resultSet.getInt(1);
				if (!result.contains(version)) result.add(version);
				int endVersion = resultSet.getInt(2);
				if (!result.contains(version)) result.add(endVersion);
			}
		}
	}
	
	// Queries
	
	protected String selectByIdAndTimeQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ? AND (startVersion = 0 OR startVersion < ?) AND (endVersion = 0 OR endVersion >= ?) ORDER BY position");
		return query.toString();
	}
	
	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); query.append(" WHERE id = ?");
		query.append(" AND endVersion = 0 ORDER BY position");
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
		s.append("id, position, startVersion, endVersion) VALUES (");
		for (int i = 0; i<getColumns().keySet().size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?, ?, 0)");

		return s.toString();
	}
	
	private String endQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE "); s.append(getTableName()); s.append(" SET endVersion = ? WHERE id = ? AND position = ? AND endVersion = 0");
		return s.toString();
	}
	
	private String readVersionsQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("SELECT startVersion, endVersion FROM "); s.append(getTableName()); 
		s.append(" WHERE id = ?");

		return s.toString();
	}
	
	protected void addSpecialColumns(DbSyntax syntax, StringBuilder s) {
		s.append(" id ");
		syntax.addColumnDefinition(s, idProperty);
		s.append(",\n startVersion INTEGER NOT NULL");
		s.append(",\n endVersion INTEGER NOT NULL");
		s.append(",\n position INTEGER NOT NULL");
	}
	
	protected void addPrimaryKey(DbSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id, startVersion, position");
	}	

}
