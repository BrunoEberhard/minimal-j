package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.minimalj.model.annotation.Grant.Privilege;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.security.Subject;
import org.minimalj.util.GenericUtils;
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
	private final String endQuery;
	private final String selectMaxVersionQuery;
	private final String readVersionsQuery;
	
	public HistorizedTable(SqlPersistence sqlPersistence, Class<T> clazz) {
		super(sqlPersistence, clazz);

		selectByIdAndTimeQuery = selectByIdAndTimeQuery();
		endQuery = endQuery();
		selectMaxVersionQuery = selectMaxVersionQuery();
		readVersionsQuery = readVersionsQuery();
	}

	@Override
	public Object insert(T object) {
		Subject.checkPermission(Privilege.INSERT, getClazz());
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, true)) {
			Object id = IdUtils.getId(object);
			if (id == null) {
				id = IdUtils.createId();
				IdUtils.setId(object, id);
			}
			setParameters(insertStatement, object, false, ParameterMode.INSERT, id);
			insertStatement.execute();
			insertLists(object);
			return id;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't insert object into " + getTableName() + " / Object: " + object + " ex: " + x);
		}
	}

	@Override
	SubTable createListTable(PropertyInterface property) {
		Class<?> elementClass = GenericUtils.getGenericClass(property.getType());
		String subTableName = buildSubTableName(property);
		if (IdUtils.hasId(elementClass)) {
			throw new RuntimeException("Not yet implemented");
		} else {
			return new HistorizedSubTable(sqlPersistence, buildSubTableName(property), elementClass, idProperty);
		}
	}
	
	@Override
	public void update(T object) {
		Object id = IdUtils.getId(object);
		update(id, object);
	}
	
	private void update(Object id, T object) {
		Subject.checkPermission(Privilege.UPDATE, getClazz());

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
			
			for (Entry<PropertyInterface, ListTable> listTableEntry : lists.entrySet()) {
				List list  = (List) listTableEntry.getKey().getValue(object);
				((HistorizedSubTable) listTableEntry.getValue()).replaceAll(object, list, version);
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
		}
	}
	
	private int findMaxVersion(Object id) {
		int result = 0;
		try (PreparedStatement selectMaxVersionStatement = createStatement(sqlPersistence.getConnection(), selectMaxVersionQuery, false)) {
			selectMaxVersionStatement.setObject(1, id);
			try (ResultSet resultSet = selectMaxVersionStatement.executeQuery()) {
				if (resultSet.next()) {
					result = resultSet.getInt(1);
				} 
				return result;
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	@Override
	public T read(Object id) {
		Subject.checkPermission(Privilege.SELECT, getClazz());
		try (PreparedStatement selectByIdStatement = createStatement(sqlPersistence.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, id);
			T object = executeSelect(selectByIdStatement);
			if (object != null) {
				loadLists(object, null);
			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id);
		}
	}

	public T read(Object id, Integer time) {
		if (time != null) {
			Subject.checkPermission(Privilege.SELECT, getClazz());
			try (PreparedStatement selectByIdAndTimeStatement = createStatement(sqlPersistence.getConnection(), selectByIdAndTimeQuery, false)) {
				selectByIdAndTimeStatement.setObject(1, id);
				selectByIdAndTimeStatement.setInt(2, time);
				T object = executeSelect(selectByIdAndTimeStatement);
				loadLists(object, time);
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
	protected void loadLists(T object) {
		loadLists(object, null);
	}
	
	@SuppressWarnings("unchecked")
	private void loadLists(T object, Integer time) {
		for (Entry<PropertyInterface, ListTable> listTableEntry : lists.entrySet()) {
			List values = ((HistorizedSubTable) listTableEntry.getValue()).read(object, time);
			PropertyInterface listProperty = listTableEntry.getKey();
			if (listProperty.isFinal()) {
				List list = (List) listProperty.getValue(object);
				list.clear();
				list.addAll(values);
			} else {
				listProperty.setValue(object, values);
			}
		}
	}		

	public List<Integer> readVersions(Object id) {
		Subject.checkPermission(Privilege.SELECT, getClazz());
		try (PreparedStatement readVersionsStatement = createStatement(sqlPersistence.getConnection(), readVersionsQuery, false)) {
			List<Integer> result = new ArrayList<Integer>();
			readVersionsStatement.setObject(1, id);
			ResultSet resultSet = readVersionsStatement.executeQuery();
			while (resultSet.next()) {
				int version = resultSet.getInt(1);
				if (!result.contains(version)) result.add(version);
			}
			resultSet.close();
			
			for (Entry<PropertyInterface, ListTable> listTableEntry : lists.entrySet()) {
				HistorizedSubTable historizedSubTable = (HistorizedSubTable) listTableEntry.getValue();
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
		query.append("SELECT * FROM ").append(getTableName()); 
		query.append(" WHERE id = ? AND version = 0");
		return query.toString();
	}
	
	@Override
	protected String selectAllQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()); 
		query.append(" WHERE version = 0");
		return query.toString();
	}
	
	protected String selectByIdAndTimeQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()); 
		query.append(" WHERE id = ? AND version = ?");
		return query.toString();
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO ").append(getTableName()).append(" (");
		for (String columnName : getColumns().keySet()) {
			s.append(columnName).append(", ");
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
		
		s.append("INSERT INTO ").append(getTableName()).append(" (");
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
		
		s.append("SELECT MAX(version) FROM ").append(getTableName()); 
		s.append(" WHERE id = ?");

		return s.toString();
	}
	
	private String endQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE ").append(getTableName()).append(" SET version = ? WHERE id = ? AND version = 0");
		return s.toString();
	}
	
	private String readVersionsQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("SELECT version FROM ").append(getTableName()); 
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
