package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Property;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * A HistorizedTable contains a column named version. In the actual valid row this
 * column is &gt; 0. After updates the row with the version -1 is the oldest row
 * the row with version -2 the second oldest and so on.
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class HistorizedTable<T> extends Table<T> {

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
	public boolean isHistorized() {
		return true;
	}
	
	@Override
	protected int setParameters(PreparedStatement statement, T object, ParameterMode mode, Object id) throws SQLException {
		HashMap<String, Property> columnsWithVersion = ((SqlHistorizedRepository) sqlRepository).findVersionColumns(clazz);
		int parameterPos = super.setParameters(statement, object, mode, id);
		for (Map.Entry<String, Property> column : columnsWithVersion.entrySet()) {
			Object referencedObject = column.getValue().getValue(object);
			if (referencedObject != null) {
				Integer version = IdUtils.getVersion(referencedObject);
				statement.setInt(parameterPos++, version);
			} else {
				statement.setInt(parameterPos++, 0);
			}
		}
		return parameterPos;
	}

	@Override
	protected SubTable createListTable(Property property) {
		Class<?> elementClass = property.getGenericClass();
		if (IdUtils.hasId(elementClass)) {
			if (FieldUtils.hasValidHistorizedField(elementClass)) {
				return new HistorizedCrossHistorizedTable(sqlRepository, buildSubTableName(property), elementClass, idProperty);
			} else {
				return new HistorizedCrossTable(sqlRepository, buildSubTableName(property), elementClass, idProperty);
			}
		} else {
			return new HistorizedSubTable(sqlRepository, buildSubTableName(property), elementClass, idProperty);
		}
	}
	
	@Override
	protected DependableTable createDependableTable(Property property, String tableName) {
		return new HistorizedDependableTable(sqlRepository, tableName, property.getClazz(), idProperty);
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
				int parameterIndex = setParameters(updateStatement, object, ParameterMode.HISTORIZE, id);
				updateStatement.setInt(parameterIndex, newVersion);
				updateStatement.execute();
			}
			
			updateDependables(object, id, newVersion);
			for (Entry<Property, ListTable> listTableEntry : lists.entrySet()) {
				List list  = (List) listTableEntry.getKey().getValue(object);
				((HistorizedListTable) listTableEntry.getValue()).replaceList(object, list, newVersion);
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
	
	public T read(Object id, int time) {
		return read(id, time, new HashMap<>());
	}
	
	public T read(Object id, int time, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		try (PreparedStatement selectByIdAndTimeStatement = createStatement(sqlRepository.getConnection(), selectByIdAndTimeQuery, false)) {
			selectByIdAndTimeStatement.setObject(1, id);
			selectByIdAndTimeStatement.setInt(2, time);
			T object = executeSelect(selectByIdAndTimeStatement, loadedReferences);
			if (object != null) {
				loadDependables(id, object, time, loadedReferences);
				loadLists(object, loadedReferences);
			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id + " in version " + time);
		}
	}

	@Override
	protected void loadLists(T object, Map<Class<?>, Map<Object, Object>> loadedReferences) throws SQLException {
		loadLists(object, (Integer) null);
	}
	
	private void loadLists(T object, Integer time) {
		for (Entry<Property, ListTable> listTableEntry : lists.entrySet()) {
			List values = ((HistorizedListTable) listTableEntry.getValue()).getList(object, time);
			Property listProperty = listTableEntry.getKey();
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
	
	private String insertQuery(boolean forUpdate) {
		HashMap<String, Property> columnsWithVersion = ((SqlHistorizedRepository) sqlRepository).findVersionColumns(clazz);

		Property idProperty = FlatProperties.getProperty(clazz, "id", true);
		boolean autoIncrementId = idProperty != null && isAutoIncrement(idProperty);

		StringBuilder s = new StringBuilder();
		s.append("INSERT INTO ").append(getTableName()).append(" (");
		for (String columnName : getColumns().keySet()) {
			s.append(columnName).append(", ");
		}
		if (forUpdate || !autoIncrementId) {
			s.append("id, ");
		}
		for (String columnName : columnsWithVersion.keySet()) {
			s.append(columnName).append(", ");
		}
		s.append("version, historized) VALUES (");
		for (int i = 0; i < getColumns().size() + columnsWithVersion.size(); i++) {
			s.append("?, ");
		}
		if (!forUpdate && autoIncrementId) {
			s.delete(s.length() - 3, s.length());
		}
		s.append(forUpdate ? "?, ?, 0)" : "?, 0, 0)");

		return s.toString();
	}
	
	@Override
	protected String insertQuery() {
		return insertQuery(false);
	}

	@Override
	protected String updateQuery() {
		return insertQuery(true);
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
	protected void addSpecialColumns(SqlDialect dialect, StringBuilder s) {
		super.addSpecialColumns(dialect, s);
		s.append(",\n historized INTEGER NOT NULL");
		HashMap<String, Property> columnsWithVersion = ((SqlHistorizedRepository) sqlRepository).findVersionColumns(clazz);
		for (String columnName : columnsWithVersion.keySet()) {
			s.append(",\n ").append(columnName).append(" INTEGER DEFAULT 0");
		}
	}
	
	@Override
	protected void addPrimaryKey(SqlDialect dialect, StringBuilder s) {
		dialect.addPrimaryKey(s, "id, version");
	}

}