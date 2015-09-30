package org.minimalj.backend.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.transaction.predicate.FieldPredicate;
import org.minimalj.transaction.predicate.PersistencePredicate.AndPredicate;
import org.minimalj.transaction.predicate.PersistencePredicate.OrPredicate;
import org.minimalj.transaction.predicate.SearchPredicate;
import org.minimalj.util.Codes;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

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
		return getTableName() + "__" + property.getName();
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

	public List<Object> whereClause(Predicate<?> predicate) {
		List<Object> result;
		if (predicate instanceof AndPredicate) {
			AndPredicate andPredicate = (AndPredicate) predicate;
			result = combine(andPredicate.getPredicate1(), andPredicate.getPredicate2(), "AND");
		} else if (predicate instanceof OrPredicate) {
			OrPredicate orPredicate = (OrPredicate) predicate;
			result = combine(orPredicate.getPredicate1(), orPredicate.getPredicate2(), "OR");
		} else if (predicate instanceof FieldPredicate) {
			FieldPredicate fieldPredicate = (FieldPredicate) predicate;
			result = new ArrayList<>();
			PropertyInterface propertyInterface = Keys.getProperty(fieldPredicate.getKey());
			Object value = fieldPredicate.getValue();
			String term = whereStatement(propertyInterface.getPath(), fieldPredicate.getOperator());
			if (ViewUtil.isReference(propertyInterface)) {
				if (!Codes.isCode(propertyInterface.getClazz()) || value == null || FieldUtils.isAllowedCodeId(value.getClass())) {
					value = IdUtils.getId(value);
				}
			}
			result.add(term);
			result.add(value);
		} else if (predicate instanceof SearchPredicate) {
			SearchPredicate searchPredicate = (SearchPredicate) predicate;
			result = new ArrayList<>();
			String search = convertUserSearch(searchPredicate.getQuery());
			String clause = "(";
			List<String> searchColumns = searchPredicate.getKeys() != null ? getColumns(searchPredicate.getKeys()) : findSearchColumns(clazz);
			boolean first = true;
			for (String column : searchColumns) {
				if (!first) {
					clause += " OR ";
				} else {
					first = false;
				}
				clause += column + (searchPredicate.isNotEqual() ? " not" : "") + " like ?";
				result.add(search);
			}
			if (this instanceof HistorizedTable) {
				clause += ") and version = 0";
			} else {
				clause += ")";
			}
			result.add(0, clause); // insert at beginning
		} else if (predicate == null) {
			result = Collections.singletonList("1=1");
		} else {
			throw new IllegalArgumentException("Unknown predicate: " + predicate);
		}
		return result;
	}
	
	private List<Object> combine(Predicate<?> predicate1, Predicate<?> predicate2, String operator) {
		List<Object> result = whereClause(predicate1);
		List<Object> result2 = whereClause(predicate2);
		String clause = "(" + result.get(0) + " " + operator + " " + result2.get(0) + ")";
		result.set(0, clause); // replace
		if (result2.size() > 1) {
			result.addAll(result2.subList(1, result2.size()));
		}
		return result;
	}
	
	public List<T> read(Predicate<?> predicate, int maxResults) {
		List<Object> whereClause = whereClause(predicate);
		String query = "SELECT * FROM " + getTableName() + " WHERE " + whereClause.get(0);
		try {
			PreparedStatement statement = getStatement(dbPersistence.getConnection(), query, false);
			for (int i = 1; i<whereClause.size(); i++) {
				helper.setParameter(statement, i, whereClause.get(i), null); // TODO property is not known here anymore. Set<enum> will fail
			}
			return executeSelectAll(statement, maxResults);
		} catch (SQLException e) {
			throw new LoggingRuntimeException(e, sqlLogger, "read with SimpleCriteria failed");
		}
	}

	public <S> List<S> readView(Class<S> resultClass, Predicate<?> predicate, int maxResults) {
		List<Object> whereClause = whereClause(predicate);
		String query = select(resultClass) + " WHERE " + whereClause.get(0);
		try {
			PreparedStatement statement = getStatement(dbPersistence.getConnection(), query, false);
			for (int i = 1; i<whereClause.size(); i++) {
				statement.setObject(i, whereClause.get(i));
			}
			return executeSelectViewAll(resultClass, statement, maxResults);
		} catch (SQLException e) {
			throw new LoggingRuntimeException(e, sqlLogger, "read with SimpleCriteria failed");
		}
	}
	
	private String select(Class<?> resultClass) {
		String querySql = "select ID";
		Map<String, PropertyInterface> propertiesByColumns = dbPersistence.findColumns(resultClass);
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
			Map<Class<?>, Map<Object, Object>> loadedReferences = new HashMap<>();
			while (resultSet.next() && result.size() < maxResults) {
				S resultObject = dbPersistence.readResultSetRow(resultClass, resultSet, loadedReferences);
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
	
	public String convertUserSearch(String s) {
		s = s.replace('*', '%');
		return s;
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
		if (searchColumns.isEmpty()) {
			throw new IllegalArgumentException("No fields are annotated as 'Searched' in " + clazz.getName());
		}
		return searchColumns;
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
	
	@Override
	protected void addPrimaryKey(DbSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id");
	}	
}
