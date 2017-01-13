package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal<p>
 *
 * A HistorizedTable contains a column named version. In the actual valid row this
 * column is &gt; 0. After updates the row with the version -1 is the oldest row
 * the row with version -2 the second oldest and so on.
 * 
 */
@SuppressWarnings("rawtypes")
public class HistorizedTable<T> extends Table<T> {

	private final String selectByIdAndTimeQuery;
	private final String endQuery;
	private final String selectMaxVersionQuery;
	
	public HistorizedTable(SqlRepository sqlRepository, Class<T> clazz) {
		super(sqlRepository, clazz);

		selectByIdAndTimeQuery = selectByIdAndTimeQuery();
		endQuery = endQuery();
		selectMaxVersionQuery = selectMaxVersionQuery();
	}

	@Override
	public Object insert(T object) {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, true)) {
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
			return new HistorizedSubTable(sqlRepository, buildSubTableName(property), elementClass, idProperty);
		}
	}
	
	@Override
	public void update(T object) {
		Object id = IdUtils.getId(object);
		update(id, object);
	}
	
	private void update(Object id, T object) {
		try {
			int version = IdUtils.getVersion(object);
			try (PreparedStatement endStatement = createStatement(sqlRepository.getConnection(), endQuery, false)) {
				endStatement.setObject(1, id);
				endStatement.setInt(2, version);
				endStatement.execute();	
				if (endStatement.getUpdateCount() == 0) {
					throw new IllegalStateException("Optimistic locking failed");
				}
			}
			
			int newVersion = version + 1;
			try (PreparedStatement updateStatement = createStatement(sqlRepository.getConnection(), updateQuery, false)) {
				int parameterIndex = setParameters(updateStatement, object, false, ParameterMode.HISTORIZE, id);
				updateStatement.setInt(parameterIndex, newVersion);
				updateStatement.execute();
			}
			
			for (Entry<PropertyInterface, ListTable> listTableEntry : lists.entrySet()) {
				List list  = (List) listTableEntry.getKey().getValue(object);
				((HistorizedSubTable) listTableEntry.getValue()).replaceAll(object, list, newVersion);
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
		}
	}
	
	public int getMaxVersion(Object id) {
		int result = 0;
		try (PreparedStatement selectMaxVersionStatement = createStatement(sqlRepository.getConnection(), selectMaxVersionQuery, false)) {
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
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
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
			try (PreparedStatement selectByIdAndTimeStatement = createStatement(sqlRepository.getConnection(), selectByIdAndTimeQuery, false)) {
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
		try (PreparedStatement deleteStatement = createStatement(sqlRepository.getConnection(), deleteQuery, false)) {
			deleteStatement.setObject(1, id);
			deleteStatement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with id " + id);
		}
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
	
	private String selectMaxVersionQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("SELECT MAX(version) FROM ").append(getTableName()); 
		s.append(" WHERE id = ?");

		return s.toString();
	}

	private String endQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE ").append(getTableName()).append(" SET historized = 1 WHERE id = ? AND version = ? AND historized = 0");
		return s.toString();
	}
	
	@Override
	protected String deleteQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE ").append(getTableName()).append(" SET historized = 1 WHERE id = ? AND historized = 0");
		return s.toString();
	}
	
	@Override
	protected void addSpecialColumns(SqlSyntax syntax, StringBuilder s) {
		super.addSpecialColumns(syntax, s);
		s.append(",\n historized INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id, version");
	}

}