package org.minimalj.backend.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.minimalj.model.PropertyInterface;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal<p>
 *
 * A HistorizedTable contains a column named version. In the actual valid row this
 * column is set to 0. After updates the row with the version 1 is the oldest row
 * the row with version 2 the second oldest and so on.<p>
 * 
 */
@SuppressWarnings("rawtypes")
public class HistorizedTable<T> extends Table<T> {

	private final String selectByIdAndTimeQuery;
	private final String updateQuery;
	private final String endQuery;
	private final String selectMaxVersionQuery;
	private final String readVersionsQuery;
	
	public HistorizedTable(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, clazz);

		selectByIdAndTimeQuery = selectByIdAndTimeQuery();
		endQuery = endQuery();
		updateQuery = updateQuery();
		selectMaxVersionQuery = selectMaxVersionQuery();
		readVersionsQuery = readVersionsQuery();
	}

	@Override
	public Object insert(T object) {
		try {
			PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, true);
			Object id = IdUtils.getId(object);
			if (id == null) {
				id = UUID.randomUUID().toString();
				IdUtils.setId(object, id);
			}
			executeInsert(insertStatement, object, id);
			for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
				HistorizedSubTable historizedSubTable = (HistorizedSubTable) subTableEntry.getValue();
				List list;
				try {
					list = (List)getLists().get(subTableEntry.getKey()).getValue(object);
					if (list != null && !list.isEmpty()) {
						historizedSubTable.insert(id, list, Integer.valueOf(0));
					}
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
			}
			return id;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't insert object into " + getTableName() + " / Object: " + object);
		}
	}

	AbstractTable createSubTable(PropertyInterface property, Class<?> clazz) {
		return new HistorizedSubTable(dbPersistence, buildSubTableName(property), clazz, idProperty);
	}
	
	@Override
	protected void update(Object id, T object) {
		// TODO Update sollte erst mal prüfen, ob update nötig ist.
		// T oldObject = read(id);
		// na, ob dann das mit allen subTables noch stimmt??
		// if (ColumnAccess.equals(oldObject, object)) return;
		
		try {
			int version = findMaxVersion(id) + 1;
			
			PreparedStatement endStatement = getStatement(dbPersistence.getConnection(), endQuery, false);
			endStatement.setInt(1, version);
			endStatement.setObject(2, id);
			endStatement.execute();	
			
			boolean doDelete = object == null;
			if (doDelete) return;
			
			PreparedStatement updateStatement = getStatement(dbPersistence.getConnection(), updateQuery, false);
			int parameterPos = setParameters(updateStatement, object, false, true);
			updateStatement.setObject(parameterPos++, id);
			updateStatement.execute();
			
			for (Entry<String, AbstractTable<?>> subTable : subTables.entrySet()) {
				HistorizedSubTable historizedSubTable = (HistorizedSubTable) subTable.getValue();
				List list;
				try {
					list = (List) getLists().get(subTable.getKey()).getValue(object);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
				historizedSubTable.update(id, list, version);
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
		}
	}
	
	private int findMaxVersion(Object id) throws SQLException {
		int result = 0;
		PreparedStatement selectMaxVersionStatement = getStatement(dbPersistence.getConnection(), selectMaxVersionQuery, false);
		selectMaxVersionStatement.setObject(1, id);
		try (ResultSet resultSet = selectMaxVersionStatement.executeQuery()) {
			if (resultSet.next()) {
				result = resultSet.getInt(1);
			} 
			return result;
		}
	}

	public T read(Object id, boolean complete) {
		try {
			PreparedStatement selectByIdStatement;
			if (complete) {
				selectByIdStatement = getStatement(dbPersistence.getConnection(), selectByIdQuery, false);
			} else {
				selectByIdStatement = createStatement(dbPersistence.getConnection(), selectByIdQuery, false);
			}
					
			selectByIdStatement.setObject(1, id);
			T object = executeSelect(selectByIdStatement);
			if (complete && object != null) {
				loadRelations(object, id, null);
			}
			if (!complete) {
				selectByIdStatement.close();
			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id);
		}
	}

	public T read(Object id, Integer time) {
		if (time != null) {
			try {
				PreparedStatement selectByIdAndTimeStatement = getStatement(dbPersistence.getConnection(), selectByIdAndTimeQuery, false);

				selectByIdAndTimeStatement.setObject(1, id);
				selectByIdAndTimeStatement.setInt(2, time);
				T object = executeSelect(selectByIdAndTimeStatement);
				loadRelations(object, id, time);
				return object;
			} catch (SQLException x) {
				throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id + " on time " +  time);
			}
		} else {
			return read(id);
		}
	}
	
	@Override
	public void delete(T object) {
		Object id = IdUtils.getId(object);
		// update to null object is delete
		update(id, null);
	}

	@Override
	protected void loadRelations(T object, Object id) throws SQLException {
		loadRelations(object, id, null);
	}
	
	@SuppressWarnings("unchecked")
	private void loadRelations(T object, Object id, Integer time) throws SQLException {
		for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
			HistorizedSubTable historizedSubTable = (HistorizedSubTable) subTableEntry.getValue();
			List list = (List)getLists().get(subTableEntry.getKey()).getValue(object);
			list.addAll(historizedSubTable.read(id, time));
		}
	}

	public List<Integer> readVersions(Object id) {
		try {
			List<Integer> result = new ArrayList<Integer>();
			
			PreparedStatement readVersionsStatement = getStatement(dbPersistence.getConnection(), readVersionsQuery, false);
			readVersionsStatement.setObject(1, id);
			ResultSet resultSet = readVersionsStatement.executeQuery();
			while (resultSet.next()) {
				int version = resultSet.getInt(1);
				if (!result.contains(version)) result.add(version);
			}
			resultSet.close();
			
			for (Entry<String, AbstractTable<?>> subTable : subTables.entrySet()) {
				HistorizedSubTable historizedSubTable = (HistorizedSubTable) subTable.getValue();
				historizedSubTable.readVersions(id, result);
			}
			
			result.remove(Integer.valueOf(0));
			Collections.sort(result);
			return result;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read version of " + getTableName() + " with ID " + id);
		}
	}
	
	
	// Statements

	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ? AND version = 0");
		return query.toString();
	}
	
	@Override
	protected String selectAllQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE version = 0");
		return query.toString();
	}
	
	protected String selectByIdAndTimeQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ? AND version = ?");
		return query.toString();
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO "); s.append(getTableName()); s.append(" (");
		for (String columnName : getColumns().keySet()) {
			s.append(columnName);
			s.append(", ");
		}
		s.append("id, version) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, 0)");

		return s.toString();
	}
	
	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO "); s.append(getTableName()); s.append(" (");
		for (String name : getColumns().keySet()) {
			s.append(name);
			s.append(", ");
		}
		s.append("id, version) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, 0)");

		return s.toString();
	}

	private String selectMaxVersionQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("SELECT MAX(version) FROM "); s.append(getTableName()); 
		s.append(" WHERE id = ?");

		return s.toString();
	}
	
	private String endQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE "); s.append(getTableName()); s.append(" SET version = ? WHERE id = ? AND version = 0");
		return s.toString();
	}
	
	private String readVersionsQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("SELECT version FROM "); s.append(getTableName()); 
		s.append(" WHERE id = ?");

		return s.toString();
	}
	
	protected void addSpecialColumns(DbSyntax syntax, StringBuilder s) {
		super.addSpecialColumns(syntax, s);
		s.append(",\n version INTEGER NOT NULL");
	}
	
	protected void addPrimaryKey(DbSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id, version");
	}

}
