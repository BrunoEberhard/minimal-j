package org.minimalj.backend.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.transaction.criteria.Criteria.AllCriteria;
import org.minimalj.transaction.criteria.Criteria.SearchCriteria;
import org.minimalj.transaction.criteria.Criteria.SimpleCriteria;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

@SuppressWarnings("rawtypes")
public class Table<T> extends AbstractTable<T> {

	protected final String selectByIdQuery;
	protected final String selectAllQuery;
	protected final String updateQuery;
	protected final String deleteQuery;
	protected final Map<String, AbstractTable<?>> subTables;
	
	public Table(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, null, clazz, FlatProperties.getProperty(clazz, "id", true));
		
		this.subTables = findSubTables();
		
		this.selectByIdQuery = selectByIdQuery();
		this.selectAllQuery = selectAllQuery();
		this.updateQuery = updateQuery();
		this.deleteQuery = deleteQuery();
	}
	
	@Override
	public void createTable(DbSyntax syntax) {
		super.createTable(syntax);
		for (AbstractTable<?> subTable : subTables.values()) {
			subTable.createTable(syntax);
		}
	}
	

	@Override
	public void createIndexes(DbSyntax syntax) {
		super.createIndexes(syntax);
		for (AbstractTable<?> subTable : subTables.values()) {
			subTable.createIndexes(syntax);
		}
	}

	@Override
	public void createConstraints(DbSyntax syntax) {
		super.createConstraints(syntax);
		for (AbstractTable<?> subTable : subTables.values()) {
			subTable.createConstraints(syntax);
		}
	}
	
	public Object insert(T object) {
		try {
			PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, true);
			Object id;
			if (IdUtils.hasId(object.getClass())) {
				id = IdUtils.getId(object);
				if (id == null) {
					id = IdUtils.createId();
					IdUtils.setId(object, id);
				}
			} else {
				id = IdUtils.createId();
			}
			setParameters(insertStatement, object, false, ParameterMode.INSERT, id);
			insertStatement.execute();
			for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
				SubTable subTable = (SubTable) subTableEntry.getValue();
				List list;
				try {
					list = (List) getLists().get(subTableEntry.getKey()).getValue(object);
					if (list != null && !list.isEmpty()) {
						subTable.insert(id, list);
					}
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
			}
			if (object instanceof Code) {
				dbPersistence.invalidateCodeCache(object.getClass());
			}
			return id;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't insert in " + getTableName() + " with " + object);
		}
	}

	public void delete(Object id) {
		PreparedStatement updateStatement;
		try {
			updateStatement = getStatement(dbPersistence.getConnection(), deleteQuery, false);
			updateStatement.setObject(1, id);
			updateStatement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't delete " + getTableName() + " with ID " + id);
		}
	}

	private Map<String, AbstractTable<?>> findSubTables() {
		Map<String, AbstractTable<?>> subTables = new HashMap<String, AbstractTable<?>>();
		Map<String, PropertyInterface> properties = getLists();
		for (PropertyInterface property : properties.values()) {
			Class<?> clazz = GenericUtils.getGenericClass(property.getType());
			subTables.put(property.getName(), createSubTable(property, clazz));
		}
		return subTables;
	}

	AbstractTable createSubTable(PropertyInterface property, Class<?> clazz) {
		return new SubTable(dbPersistence, buildSubTableName(property), clazz, idProperty);
	}

	protected String buildSubTableName(PropertyInterface property) {
		StringBuilder b = new StringBuilder();
		b.append(getTableName());
		String fieldName = StringUtils.upperFirstChar(property.getName());
		b.append('_'); b.append(fieldName); 
		return b.toString();
	}
	
	public void update(T object) {
		Object id = IdUtils.getId(object);
		update(id, object);
	}

	protected void update(Object id, T object) {
		try {
			PreparedStatement updateStatement = getStatement(dbPersistence.getConnection(), updateQuery, false);
			setParameters(updateStatement, object, false, ParameterMode.UPDATE, id);
			updateStatement.execute();
			
			for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
				SubTable subTable = (SubTable) subTableEntry.getValue();
				List list;
				try {
					list = (List) getLists().get(subTableEntry.getKey()).getValue(object);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
				subTable.update(id, list);
			}
			
			if (object instanceof Code) {
				dbPersistence.invalidateCodeCache(object.getClass());
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
		}
	}
	
	public T read(Object id) {
		return read(id, true);
	}
	
	protected T read(Object id, boolean complete) {
		try {
			PreparedStatement selectByIdStatement = getStatement(dbPersistence.getConnection(), selectByIdQuery, false);
			selectByIdStatement.setObject(1, id);
			T object = executeSelect(selectByIdStatement);
			if (complete && object != null) {
				loadRelations(object, id);
			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id);
		}
	}

	private List<String> getColumns(Object[] keys) {
		List<String> result = new ArrayList<>();
		PropertyInterface[] properties = Keys.getProperties(keys);
		for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
			PropertyInterface property = entry.getValue();
			for (PropertyInterface p : properties) {
				if (p.getPath().equals(property.getPath())) {
					result.add(entry.getKey());
				}
			}
		}
		return result;
	}

	public List<T> read(Criteria criteria, int maxResults) {
		String query = "select * from " + getTableName();
		if (criteria instanceof SimpleCriteria) {
			SimpleCriteria simpleCriteria = (SimpleCriteria) criteria;
			PropertyInterface propertyInterface = Keys.getProperty(simpleCriteria.getKey());
			query += " where " + whereStatement(propertyInterface.getPath(), simpleCriteria.getOperator());
			try {
				PreparedStatement statement = getStatement(dbPersistence.getConnection(), query, false);
				Object value = simpleCriteria.getValue();
				if (!(value instanceof Integer || value instanceof Long) && ViewUtil.isReference(propertyInterface)) {
					value = IdUtils.getId(value);
				}
				statement.setObject(1, value);
				return executeSelectAll(statement, (long)maxResults);
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "read with SimpleCriteria failed");
			}
		} else if (criteria instanceof SearchCriteria) {
			SearchCriteria searchCriteria = (SearchCriteria) criteria;

			query += " where (";
			List<String> searchColumns = searchCriteria.getKeys() != null ? getColumns(searchCriteria.getKeys()) : findSearchColumns(clazz);
			boolean first = true;
			for (String column : searchColumns) {
				if (!first) {
					query += " OR ";
				} else {
					first = false;
				}
				query += column + " like ?";
			}
			if (this instanceof HistorizedTable) {
				query += ") and version = 0";
			} else {
				query += ")";
			}

			try {
				PreparedStatement statement = getStatement(dbPersistence.getConnection(), query, false);
				for (int i = 0; i<searchColumns.size(); i++) {
					statement.setString(i+1, searchCriteria.getQuery());
				}
				return executeSelectAll(statement);
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "read with SimpleCriteria failed");
			}
		} else if (criteria instanceof AllCriteria) {
			try {
				PreparedStatement statement = getStatement(dbPersistence.getConnection(), query, false);
				return executeSelectAll(statement, maxResults);
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "read with MaxResultsCriteria failed");
			}
		}
		throw new IllegalArgumentException(criteria + " not yet implemented");
	}

	public <S> List<S> readView(Class<S> resultClass, Criteria criteria, int maxResults) {
		String query = select(resultClass);
		if (criteria instanceof SimpleCriteria) {
			SimpleCriteria simpleCriteria = (SimpleCriteria) criteria;
			PropertyInterface propertyInterface = Keys.getProperty(simpleCriteria.getKey());
			query += " where " + whereStatement(propertyInterface.getPath(), simpleCriteria.getOperator());
			try {
				PreparedStatement statement = getStatement(dbPersistence.getConnection(), query, false);
				Object value = simpleCriteria.getValue();
				if (!(value instanceof Integer || value instanceof Long) && ViewUtil.isReference(propertyInterface)) {
					value = IdUtils.getId(value);
				}
				statement.setObject(1, value);
				return executeSelectViewAll(resultClass, statement, maxResults);
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "read with SimpleCriteria failed");
			}
		} else if (criteria instanceof SearchCriteria) {
			SearchCriteria searchCriteria = (SearchCriteria) criteria;

			query += " where (";
			List<String> searchColumns = searchCriteria.getKeys() != null ? getColumns(searchCriteria.getKeys()) : findSearchColumns(clazz);
			boolean first = true;
			for (String column : searchColumns) {
				if (!first) {
					query += " OR ";
				} else {
					first = false;
				}
				query += column + " like ?";
			}
			if (this instanceof HistorizedTable) {
				query += ") and version = 0";
			} else {
				query += ")";
			}

			try (PreparedStatement statement = createStatement(dbPersistence.getConnection(), query, false)) {
				for (int i = 0; i<searchColumns.size(); i++) {
					statement.setString(i+1, searchCriteria.getQuery());
				}
				return executeSelectViewAll(resultClass, statement, maxResults);
			} catch (Exception e) {
				throw new LoggingRuntimeException(e, sqlLogger, "read with MaxResultsCriteria failed");
			}
		} else if (criteria instanceof AllCriteria) {
			try {
				PreparedStatement statement = getStatement(dbPersistence.getConnection(), query, false);
				return executeSelectViewAll(resultClass, statement, maxResults);
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "read with MaxResultsCriteria failed");
			}
		}
		throw new IllegalArgumentException(criteria + " not yet implemented");
	}

	private String select(Class<?> resultClass) {
		String querySql = "select ID";
		Map<String, PropertyInterface> propertiesByColumns = findColumns(resultClass);
		for (String column : propertiesByColumns.keySet()) {
			querySql += ", ";
			querySql += column;
		}
		querySql += " from " + getTableName();
		return querySql;
	}
	
	protected <S> List<S> executeSelectViewAll(Class<S> resultClass, PreparedStatement preparedStatement, long maxResults) throws SQLException {
		List<S> result = new ArrayList<>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next() && result.size() < maxResults) {
				S resultObject = (S) readResultSetRow(dbPersistence, (Class<S>) resultClass, resultSet, 0);
				result.add(resultObject);

				Object id = IdUtils.getId(resultObject);
				LinkedHashMap<String, PropertyInterface> lists = findLists(resultClass);
				for (String listField : lists.keySet()) {
					List list = (List) lists.get(listField).getValue(resultObject);
					if (subTables.get(listField) instanceof SubTable) {
						SubTable subTable = (SubTable) subTables.get(listField);
						list.addAll(subTable.read(id));
					} else if (subTables.get(listField) instanceof HistorizedSubTable) {
						HistorizedSubTable subTable = (HistorizedSubTable) subTables.get(listField);
						list.addAll(subTable.read(id, 0));
					}
				}
			}
		}
		return result;
	}
	
	public List<T> search(String query, int maxResults) {
		List<String> searchColumns = findSearchColumns(clazz);
		return search(searchColumns, query, maxResults);
	}
	
	public List<T> search(Object[] keys, String query, int maxResults) {
		return search(getColumns(keys), query, maxResults);
	}

	public <S extends View<T>> List<S> search(Class<S> viewClass, Object[] keys, String query, int maxResults) {
		return search(viewClass, getColumns(keys), query, maxResults);
	}
	
	public <S extends View<?>> List<S> search(Class<S> viewClass, String query, int maxResults) {
		List<String> searchColumns = findSearchColumns(clazz);
		return search(viewClass, searchColumns, query, maxResults);
	}

	private List<String> findSearchColumns(Class<?> clazz) {
		List<String> searchColumns = new ArrayList<>();
		for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
			PropertyInterface property = entry.getValue();
			Searched searchable = property.getAnnotation(Searched.class);
			if (searchable != null) {
				searchColumns.add(entry.getKey());
			}
		}
		return searchColumns;
	}
	
	private List<T> search(List<String> searchColumns, String query, int maxResults) {
		String querySql = "select * from " + getTableName() + " where (";
		
		boolean first = true;
		for (String column : searchColumns) {
			if (!first) {
				querySql += " OR ";
			} else {
				first = false;
			}
			querySql += column + " like ?";
		}
		if (this instanceof HistorizedTable) {
			querySql += ") and version = 0";
		} else {
			querySql += ")";
		}

		List<T> result = new ArrayList<>();
		try {
			PreparedStatement statement = createStatement(dbPersistence.getConnection(), querySql, false);
			for (int i = 0; i<searchColumns.size(); i++) {
				statement.setString(i+1, query);
			}
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next() && result.size() < maxResults) {
					T resultObject = (T) readResultSetRow(dbPersistence, (Class<T>) clazz, resultSet, 0);
					result.add(resultObject);

					Object id = IdUtils.getId(resultObject);
					loadRelations((T) resultObject, id);
				}
			}
			return result;
		} catch (Exception e) {
			throw new LoggingRuntimeException(e, sqlLogger, "read with MaxResultsCriteria failed");
		}
	}	
	
	private <S> List<S> search(Class<S> resultClass, List<String> searchColumns, String query, int maxResults) {
		String columns = "";
			Map<String, PropertyInterface> propertiesByColumns = findColumns(resultClass);
			columns = "ID";
			for (String column : propertiesByColumns.keySet()) {
				columns += ", ";
				columns += column;
			}
		String querySql = "select " + columns + " from " + getTableName() + " where (";
		
		boolean first = true;
		for (String column : searchColumns) {
			if (!first) {
				querySql += " OR ";
			} else {
				first = false;
			}
			querySql += column + " like ?";
		}
		if (this instanceof HistorizedTable) {
			querySql += ") and version = 0";
		} else {
			querySql += ")";
		}

		List<S> result = new ArrayList<>();
		try {
			PreparedStatement statement = createStatement(dbPersistence.getConnection(), querySql, false);
			for (int i = 0; i<searchColumns.size(); i++) {
				statement.setString(i+1, query);
			}
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next() && result.size() < maxResults) {
					S resultObject = (S) readResultSetRow(dbPersistence, (Class<S>) resultClass, resultSet, 0);
					result.add(resultObject);

					Object id = IdUtils.getId(resultObject);
					LinkedHashMap<String, PropertyInterface> lists = findLists(resultClass);
					for (String listField : lists.keySet()) {
						List list = (List) lists.get(listField).getValue(resultObject);
						if (subTables.get(listField) instanceof SubTable) {
							SubTable subTable = (SubTable) subTables.get(listField);
							list.addAll(subTable.read(id));
						} else if (subTables.get(listField) instanceof HistorizedSubTable) {
							HistorizedSubTable subTable = (HistorizedSubTable) subTables.get(listField);
							list.addAll(subTable.read(id, 0));
						}
					}
				}
			}
			return result;
		} catch (Exception e) {
			throw new LoggingRuntimeException(e, sqlLogger, "read with MaxResultsCriteria failed");
		}
	}	

	@SuppressWarnings("unchecked")
	protected void loadRelations(T object, Object id) throws SQLException {
		for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
			SubTable subTable = (SubTable) subTableEntry.getValue();
			List list = (List) getLists().get(subTableEntry.getKey()).getValue(object);
			list.addAll(subTable.read(id));
		}
	}
	
	// Statements

	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ?");
		return query.toString();
	}
	
	protected String selectAllQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
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
		s.append("id) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?)");

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
		s.append(" WHERE id = ?");

		return s.toString();
	}
	
	protected String deleteQuery() {
		StringBuilder s = new StringBuilder();
		s.append("DELETE FROM "); s.append(getTableName()); s.append(" WHERE id = ?");
		return s.toString();
	}
	
	@Override
	protected void addSpecialColumns(DbSyntax syntax, StringBuilder s) {
		if (idProperty != null) {
			syntax.addIdColumn(s, idProperty);
		} else {
			syntax.addIdColumn(s, Object.class, 36);
		}
	}
	
	protected void addPrimaryKey(DbSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id");
	}	
}
