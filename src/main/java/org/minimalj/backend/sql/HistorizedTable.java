package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal<p>
 *
 * A HistorizedTable contains a column named version. In the actual valid row this
 * column is set to 0. After updates the row with the version 1 is the oldest row
 * the row with version 2 the second oldest and so on.
 * 
 */
@SuppressWarnings("rawtypes")
public class HistorizedTable<T> extends Table<T> {

	private final String selectByIdAndTimeQuery;
	private final String updateQuery;
	private final String endQuery;
	private final String selectMaxVersionQuery;
	private final String readVersionsQuery;
	
	public HistorizedTable(SqlPersistence sqlPersistence, Class<T> clazz) {
		super(sqlPersistence, clazz);

		selectByIdAndTimeQuery = selectByIdAndTimeQuery();
		endQuery = endQuery();
		updateQuery = updateQuery();
		selectMaxVersionQuery = selectMaxVersionQuery();
		readVersionsQuery = readVersionsQuery();
	}

	@Override
	public Object insert(T object) {
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, true)) {
			Object id = IdUtils.getId(object);
			if (id == null) {
				id = IdUtils.createId();
				IdUtils.setId(object, id);
			}
			setParameters(insertStatement, object, false, ParameterMode.INSERT, id);
			insertStatement.execute();
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
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't insert object into " + getTableName() + " / Object: " + object + " ex: " + x);
		}
	}
	
	@Override
	AbstractTable createSubTable(PropertyInterface property, Class<?> clazz) {
		return new HistorizedSubTable(sqlPersistence, buildSubTableName(property), clazz, idProperty);
	}
	
	@Override
	AbstractTable createViewSubTable(PropertyInterface property, Class<?> viewClass) {
		return new HistorizedViewSubTable(sqlPersistence, buildSubTableName(property), viewClass, idProperty);
	}
	
	@Override
	protected void update(Object id, T object) {
		// TODO Update sollte erst mal prüfen, ob update nötig ist.
		// T oldObject = read(id);
		// na, ob dann das mit allen subTables noch stimmt??
		// if (ColumnAccess.equals(oldObject, object)) return;
		
		try {
			int version = findMaxVersion(id) + 1;
			
			try (PreparedStatement endStatement = createStatement(sqlPersistence.getConnection(), endQuery, false)) {
				endStatement.setInt(1, version);
				endStatement.setObject(2, id);
				endStatement.execute();	
			}
			
			boolean doDelete = object == null;
			if (doDelete) return;
			
			try (PreparedStatement updateStatement = createStatement(sqlPersistence.getConnection(), updateQuery, false)) {
				setParameters(updateStatement, object, false, ParameterMode.HISTORIZE, id);
				updateStatement.execute();
			}
			
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
		try (PreparedStatement selectMaxVersionStatement = createStatement(sqlPersistence.getConnection(), selectMaxVersionQuery, false)) {
			selectMaxVersionStatement.setObject(1, id);
			try (ResultSet resultSet = selectMaxVersionStatement.executeQuery()) {
				if (resultSet.next()) {
					result = resultSet.getInt(1);
				} 
				return result;
			}
		}
	}

	@Override
	public T read(Object id, boolean complete) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlPersistence.getConnection(), selectByIdQuery, false)) {
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
			try (PreparedStatement selectByIdAndTimeStatement = createStatement(sqlPersistence.getConnection(), selectByIdAndTimeQuery, false)) {
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
	public void delete(Object id) {
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
			PropertyInterface listProperty = getLists().get(subTableEntry.getKey());
			if (listProperty.isFinal()) {
				List list = (List) listProperty.getValue(object);
				list.addAll(historizedSubTable.read(id, time));
			} else {
				listProperty.setValue(object, historizedSubTable.read(id, time));
			}
		}
	}

	public List<Integer> readVersions(Object id) {
		try (PreparedStatement readVersionsStatement = createStatement(sqlPersistence.getConnection(), readVersionsQuery, false)) {
			List<Integer> result = new ArrayList<Integer>();
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
	
	@Override
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
	
	@Override
	protected void addSpecialColumns(SqlSyntax syntax, StringBuilder s) {
		super.addSpecialColumns(syntax, s);
		s.append(",\n version INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id, version");
	}

}
