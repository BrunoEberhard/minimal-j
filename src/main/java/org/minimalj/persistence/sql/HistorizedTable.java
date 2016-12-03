package org.minimalj.persistence.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

import org.minimalj.model.annotation.Grant.Privilege;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.security.Authorization;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal<p>
 *
 * A HistorizedTable contains a column named version. In the actual valid row this
 * column is > 0. After updates the row with the version -1 is the oldest row
 * the row with version -2 the second oldest and so on.
 * 
 */
@SuppressWarnings("rawtypes")
public class HistorizedTable<T> extends Table<T> {

	private final String selectByIdAndTimeQuery;
	private final String endQuery;
	
	public HistorizedTable(SqlPersistence sqlPersistence, Class<T> clazz) {
		super(sqlPersistence, clazz);

		selectByIdAndTimeQuery = selectByIdAndTimeQuery();
		endQuery = endQuery();
	}

	@Override
	public Object insert(T object) {
		Authorization.checkGrants(Privilege.INSERT, getClazz());
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
		Authorization.checkGrants(Privilege.UPDATE, getClazz());

		// TODO Update sollte erst mal prüfen, ob update nötig ist.
		// T oldObject = read(id);
		// na, ob dann das mit allen subTables noch stimmt??
		// if (ColumnAccess.equals(oldObject, object)) return;
		
		try {
			try (PreparedStatement endStatement = createStatement(sqlPersistence.getConnection(), endQuery, false)) {
				endStatement.setObject(1, id);
				endStatement.execute();	
			}
			
			boolean doDelete = object == null;
			if (doDelete) return;
			
			int version = IdUtils.getVersion(object) + 1;
			IdUtils.setVersion(object, version);
			try (PreparedStatement updateStatement = createStatement(sqlPersistence.getConnection(), updateQuery, false)) {
				int parameterIndex = setParameters(updateStatement, object, false, ParameterMode.HISTORIZE, id);
				updateStatement.setInt(parameterIndex, version);
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
	
	@Override
	public T read(Object id) {
		Authorization.checkGrants(Privilege.SELECT, getClazz());
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
			Authorization.checkGrants(Privilege.SELECT, getClazz());
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
	
	// Statements

	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()); 
		query.append(" WHERE id = ? AND historized = 0");
		return query.toString();
	}
	
	@Override
	protected String selectAllQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()); 
		query.append(" WHERE historized = 0");
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
		s.append("id, version, historized) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, 0, 0)");

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
		s.append("id, version, historized) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?, 0)");

		return s.toString();
	}

	private String endQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE ").append(getTableName()).append(" SET historized = 1 WHERE id = ? AND historized = 0");
		return s.toString();
	}
	
	@Override
	protected void addSpecialColumns(SqlSyntax syntax, StringBuilder s) {
		super.addSpecialColumns(syntax, s);
		s.append(",\n version INTEGER NOT NULL");
		s.append(",\n historized INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id, version");
	}

}