package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.Keys.MethodProperty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.list.RelationCriteria;
import org.minimalj.repository.query.AllCriteria;
import org.minimalj.repository.query.Criteria.AndCriteria;
import org.minimalj.repository.query.Criteria.OrCriteria;
import org.minimalj.repository.query.FieldCriteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.query.SearchCriteria;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

@SuppressWarnings("rawtypes")
public class Table<T> extends AbstractTable<T> {
	
	private static final List<Object> EMPTY_WHERE_CLAUSE = Collections.singletonList("1=1");
	
	protected final PropertyInterface idProperty;
	protected final boolean optimisticLocking;
	
	protected final String selectAllQuery;

	protected final HashMap<PropertyInterface, ListTable> lists;
	
	public Table(SqlRepository sqlRepository, Class<T> clazz) {
		this(sqlRepository, null, clazz);
	}
	
	public Table(SqlRepository sqlRepository, String name, Class<T> clazz) {
		super(sqlRepository, name, clazz);
		
		this.idProperty = FlatProperties.getProperty(clazz, "id", true);
		Objects.nonNull(idProperty);
		
		this.optimisticLocking = FieldUtils.hasValidVersionfield(clazz);

		List<PropertyInterface> lists = FlatProperties.getListProperties(clazz);
		this.lists = createListTables(lists);
		
		this.selectAllQuery = selectAllQuery();
	}
	
	@Override
	public void createTable(SqlDialect dialect) {
		super.createTable(dialect);
		for (Object object : lists.values()) {
			AbstractTable subTable = (AbstractTable) object;
			subTable.createTable(dialect);
		}
	}

	@Override
	public void createIndexes(SqlDialect dialect) {
		super.createIndexes(dialect);
		for (Object object : lists.values()) {
			AbstractTable subTable = (AbstractTable) object;
			subTable.createIndexes(dialect);
		}
	}

	@Override
	public void createConstraints(SqlDialect dialect) {
		super.createConstraints(dialect);
		for (Object object : lists.values()) {
			AbstractTable subTable = (AbstractTable) object;
			subTable.createConstraints(dialect);
		}
	}
	
	protected Object createId() {
		return UUID.randomUUID().toString();
	}
	
	public Object insert(T object) {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, true)) {
			Object id;
			if (IdUtils.hasId(object.getClass())) {
				id = IdUtils.getId(object);
				if (id == null) {
					id = createId();
					IdUtils.setId(object, id);
				}
			} else {
				id = createId();
			}
			setParameters(insertStatement, object, false, ParameterMode.INSERT, id);
			insertStatement.execute();
			insertLists(object);
			if (object instanceof Code) {
				sqlRepository.invalidateCodeCache(object.getClass());
			}
			return id;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't insert in " + getTableName() + " with " + object);
		}
	}

	protected void insertLists(T object) {
		for (Entry<PropertyInterface, ListTable> listEntry : lists.entrySet()) {
			List list = (List) listEntry.getKey().getValue(object);
			if (list != null && !list.isEmpty()) {
				listEntry.getValue().addList(object, list);
			}
		}
	}

	public void delete(Object id) {
		try (PreparedStatement updateStatement = createStatement(sqlRepository.getConnection(), deleteQuery, false)) {
			updateStatement.setObject(1, id);
			updateStatement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't delete " + getTableName() + " with ID " + id);
		}
	}
	
	private LinkedHashMap<PropertyInterface, ListTable> createListTables(List<PropertyInterface> listProperties) {
		LinkedHashMap<PropertyInterface, ListTable> lists = new LinkedHashMap<>();
		for (PropertyInterface listProperty : listProperties) {
			ListTable listTable = createListTable(listProperty);
			lists.put(listProperty, listTable);
		}
		return lists;
	}

	ListTable createListTable(PropertyInterface property) {
		Class<?> elementClass = GenericUtils.getGenericClass(property.getType());
		String subTableName = buildSubTableName(property);
		if (IdUtils.hasId(elementClass)) {
			return new CrossTable<>(sqlRepository, subTableName, elementClass, idProperty);
		} else {
			return new SubTable(sqlRepository, subTableName, elementClass, idProperty);
		}
	}
	
	protected String buildSubTableName(PropertyInterface property) {
		return getTableName() + "__" + property.getName();
	}
	
	public void update(T object) {
		updateWithId(object, IdUtils.getId(object));
	}
	
	void updateWithId(T object, Object id) {
		try (PreparedStatement updateStatement = createStatement(sqlRepository.getConnection(), updateQuery, false)) {
			int parameterIndex = setParameters(updateStatement, object, false, ParameterMode.UPDATE, id);
			if (optimisticLocking) {
				updateStatement.setInt(parameterIndex, IdUtils.getVersion(object));
				updateStatement.execute();
				if (updateStatement.getUpdateCount() == 0) {
					throw new IllegalStateException("Optimistic locking failed");
				}
			} else {
				updateStatement.execute();
			}
			
			for (Entry<PropertyInterface, ListTable> listTableEntry : lists.entrySet()) {
				List list  = (List) listTableEntry.getKey().getValue(object);
				listTableEntry.getValue().replaceList(object, list);
			}
			
			if (object instanceof Code) {
				sqlRepository.invalidateCodeCache(object.getClass());
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
		}
	}

	public T read(Object id) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, id);
			T object = executeSelect(selectByIdStatement);
			if (object != null) {
				loadLists(object);
			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id);
		}
	}

	public T read(Object id, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, id);
			T object = executeSelect(selectByIdStatement, loadedReferences);
			if (object != null) {
				loadLists(object);
			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id);
		}
	}
	
	private List<String> getColumns(Object[] keys) {
		List<String> result = new ArrayList<>();
		PropertyInterface[] properties = Keys.getProperties(keys);
		for (PropertyInterface p : properties) {
			if (p instanceof MethodProperty) {
				throw new IllegalArgumentException("Not possible to query for method properties");
			} else if (p.getPath().equals("id")) {
				result.add("id");
				continue;
			}
			for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
				PropertyInterface property = entry.getValue();
				if (p.getPath().equals(property.getPath())) {
					result.add(entry.getKey());
				}
			}
		}
		return result;
	}

	public List<Object> whereClause(Query query) {
		List<Object> result;
		if (query instanceof AndCriteria) {
			AndCriteria andCriteria = (AndCriteria) query;
			result = combine(andCriteria.getCriterias(), "AND");
		} else if (query instanceof OrCriteria) {
			OrCriteria orCriteria = (OrCriteria) query;
			result = combine(orCriteria.getCriterias(), "OR");
		} else if (query instanceof FieldCriteria) {
			FieldCriteria fieldCriteria = (FieldCriteria) query;
			result = new ArrayList<>();
			Object value = fieldCriteria.getValue();
			String term = whereStatement(fieldCriteria.getPath(), fieldCriteria.getOperator());
			if (value != null && IdUtils.hasId(value.getClass())) {
				value = IdUtils.getId(value);
			}
			result.add(term);
			result.add(value);
		} else if (query instanceof SearchCriteria) {
			SearchCriteria searchCriteria = (SearchCriteria) query;
			result = new ArrayList<>();
			String search = convertUserSearch(searchCriteria.getQuery());
			String clause = "(";
			List<String> searchColumns = searchCriteria.getKeys() != null ? getColumns(searchCriteria.getKeys()) : findSearchColumns(clazz);
			boolean first = true;
			for (String column : searchColumns) {
				if (!first) {
					clause += " OR ";
				} else {
					first = false;
				}
				clause += column + (searchCriteria.isNotEqual() ? " NOT" : "") + " LIKE ?";
				result.add(search);
			}
			clause += ")";
			if (isHistorized()) {
				clause += " and historized = 0";
			}
			result.add(0, clause); // insert at beginning
		} else if (query instanceof RelationCriteria) {
			RelationCriteria relationCriteria = (RelationCriteria) query;
			result = new ArrayList<>();
			String crossTableName = relationCriteria.getCrossName();
			avoidSqlInjection(crossTableName);
			String clause = "T.id = C.elementId AND C.id = ? ORDER BY C.position";
			result.add(clause);
			result.add(relationCriteria.getRelatedId());
		} else if (query instanceof Limit) {
			Limit limit = (Limit) query;
			result = whereClause(limit.getQuery());
			String s = (String) result.get(0);
			s = s + " " + sqlRepository.getSqlDialect().limit(limit.getRows(), limit.getOffset());
			result.set(0, s);
		} else if (query instanceof Order) {
			Order order = (Order) query;
			List<Order> orders = new ArrayList<>();
			orders.add(order);
			while (order.getQuery() instanceof Order) {
				order = (Order) order.getQuery();
				orders.add(order);
			}
			result = whereClause(order.getQuery());
			String s = (String) result.get(0);
			s = s + " " + order(orders);
			result.set(0, s);
		} else if (query instanceof AllCriteria) {
			result = new ArrayList<>(EMPTY_WHERE_CLAUSE);
		} else if (query == null) {
			result = EMPTY_WHERE_CLAUSE;
		} else {
			throw new IllegalArgumentException("Unknown criteria: " + query);
		}
		return result;
	}

	private void avoidSqlInjection(String crossTableName) {
		if (!sqlRepository.getTableByName().containsKey(crossTableName)) {
			throw new IllegalArgumentException("Invalid cross name: " + crossTableName);
		}
	}
	
	private String order(List<Order> orders) {
		StringBuilder s = new StringBuilder();
		for (Order order : orders) {
			if (s.length() == 0) {
				s.append("ORDER BY ");
			} else {
				s.append(", ");
			}
			s.append(findColumn(order.getPath()));
			if (!order.isAscending()) {
				s.append(" DESC");
			}
		}
		return s.toString();
	}
	
	private List<Object> combine(List<? extends Query> criterias, String operator) {
		if (criterias.isEmpty()) {
			return null;
		} else if (criterias.size() == 1) {
			return whereClause(criterias.get(0));
		} else {
			List<Object> whereClause = whereClause(criterias.get(0));
			String clause = "(" + whereClause.get(0);
			for (int i = 1; i<criterias.size(); i++) {
				List<Object> whereClause2 = whereClause(criterias.get(i));
				clause += " " + operator + " " + whereClause2.get(0);
				if (whereClause2.size() > 1) {
					whereClause.addAll(whereClause2.subList(1, whereClause2.size()));
				}
			}
			clause += ")";
			whereClause.set(0, clause); // replace
			return whereClause;
		}
	}
	
	public long count(Query query) {
		query = getCriteria(query);
		String tableName;
		List<Object> whereClause;
		if (query instanceof RelationCriteria) {
			RelationCriteria relationCriteria = (RelationCriteria) query;
			tableName = relationCriteria.getCrossName();
			avoidSqlInjection(tableName);
			whereClause = Arrays.asList("id = ?", relationCriteria.getRelatedId());
		} else {
			tableName = getTableName();
			whereClause = whereClause(query);
		}
		String queryString = "SELECT COUNT(*) FROM " + tableName + (whereClause != EMPTY_WHERE_CLAUSE ? " WHERE " + whereClause.get(0) : "");
		try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), queryString, false)) {
			for (int i = 1; i<whereClause.size(); i++) {
				sqlRepository.getSqlDialect().setParameter(statement, i, whereClause.get(i), null); // TODO property is not known here anymore. Set<enum> will fail
			}
			return executeSelectCount(statement);
		} catch (SQLException e) {
			throw new LoggingRuntimeException(e, sqlLogger, "count failed");
		}
	}

	private Query getCriteria(Query query) {
		if (query instanceof Limit) {
			query = ((Limit) query).getQuery();
		}
		while (query instanceof Order) {
			query = ((Order) query).getQuery();
		}
		return query;
	}
	
	public <S> List<S> find(Query query, Class<S> resultClass) {
		List<Object> whereClause = whereClause(query);
		String select = getCriteria(query) instanceof RelationCriteria ? select(resultClass, (RelationCriteria) getCriteria(query)) : select(resultClass);
		String queryString = select + (whereClause != EMPTY_WHERE_CLAUSE ? " WHERE " + whereClause.get(0) : "");
		try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), queryString, false)) {
			for (int i = 1; i<whereClause.size(); i++) {
				sqlRepository.getSqlDialect().setParameter(statement, i, whereClause.get(i), null); // TODO property is not known here anymore. Set<enum> will fail
			}
			return resultClass == getClazz() ? (List<S>) executeSelectAll(statement) : executeSelectViewAll(resultClass, statement);
		} catch (SQLException e) {
			throw new LoggingRuntimeException(e, sqlLogger, "read with SimpleCriteria failed");
		}
	}

	public <S> S readView(Class<S> resultClass, Object id, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		String query = select(resultClass) + " WHERE id = ?";
		try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), query, false)) {
			statement.setObject(1, id);
			return executeSelectView(resultClass, statement, loadedReferences);
		} catch (SQLException e) {
			throw new LoggingRuntimeException(e, sqlLogger, "read with SimpleCriteria failed");
		}
	}

	private String select(Class<?> resultClass) {
		String querySql = "SELECT ";
		if (resultClass == getClazz()) {
			querySql += "*";
		} else {
			querySql += "id";
			Map<String, PropertyInterface> propertiesByColumns = sqlRepository.findColumns(resultClass);
			for (String column : propertiesByColumns.keySet()) {
				querySql += ", "+ column;
			}
		}
		querySql += " FROM " + getTableName();
		return querySql;
	}
	
	private String select(Class<?> resultClass, RelationCriteria relationCriteria) {
		String crossTableName = relationCriteria.getCrossName();
		String querySql = "SELECT ";
		if (resultClass == getClazz()) {
			querySql += "T.*";
		} else {
			querySql += "T.id";
			Map<String, PropertyInterface> propertiesByColumns = sqlRepository.findColumns(resultClass);
			for (String column : propertiesByColumns.keySet()) {
				querySql += ", T." + column;
			}
		}
		querySql += " FROM " + getTableName() + " T, " + crossTableName + " C";
		return querySql;
	}
	
	protected <S> List<S> executeSelectViewAll(Class<S> resultClass, PreparedStatement preparedStatement) throws SQLException {
		List<S> result = new ArrayList<>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			Map<Class<?>, Map<Object, Object>> loadedReferences = new HashMap<>();
			while (resultSet.next()) {
				S resultObject = sqlRepository.readResultSetRow(resultClass, resultSet, loadedReferences);
				loadViewLists(resultObject);
				result.add(resultObject);
			}
		}
		return result;
	}

	protected <S> S executeSelectView(Class<S> resultClass, PreparedStatement preparedStatement, Map<Class<?>, Map<Object, Object>> loadedReferences) throws SQLException {
		S result = null;
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				result = sqlRepository.readResultSetRow(resultClass, resultSet, loadedReferences);
				loadViewLists(result);				
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
	protected void loadLists(T object) throws SQLException {
		for (Entry<PropertyInterface, ListTable> listTableEntry : lists.entrySet()) {
			List values = listTableEntry.getValue().getList(object);
			PropertyInterface listProperty = listTableEntry.getKey();
			listProperty.setValue(object, values);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <S> void loadViewLists(S result) {
		List<PropertyInterface> viewLists = FlatProperties.getListProperties(result.getClass());
		for (PropertyInterface viewListProperty : viewLists) {
			for (Entry<PropertyInterface, ListTable> listPropertyEntry : lists.entrySet()) {
				if (viewListProperty.getPath().equals(listPropertyEntry.getKey().getPath())) {
					List values = listPropertyEntry.getValue().getList(result);
					viewListProperty.setValue(result, values);

					break;
				}
			}
		}
	}
	
	// Statements

	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()).append(" WHERE id = ?");
		return query.toString();
	}
	
	protected String selectAllQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()); 
		return query.toString();
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO ").append(getTableName()).append(" (");
		for (String columnName : getColumns().keySet()) {
			s.append(columnName).append(", ");
		}
		s.append("id) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?)");

		return s.toString();
	}
	
	@Override
	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("UPDATE ").append(getTableName()).append(" SET ");
		for (Object columnNameObject : getColumns().keySet()) {
			s.append((String) columnNameObject).append("= ?, ");
		}
		// this is used in a callback where OptimisticLocking is not yet initialized
		boolean optimisticLocking = FieldUtils.hasValidVersionfield(clazz);
		if (optimisticLocking) {
			s.append(" version = version + 1 WHERE id = ? AND version = ?");
		} else {
			s.delete(s.length()-2, s.length());
			s.append(" WHERE id = ?");
		}

		return s.toString();
	}
	
	@Override
	protected String deleteQuery() {
		StringBuilder s = new StringBuilder();
		s.append("DELETE FROM ").append(getTableName()).append(" WHERE id = ?");
		return s.toString();
	}
	
	@Override
	protected void addSpecialColumns(SqlDialect dialect, StringBuilder s) {
		if (idProperty != null) {
			dialect.addIdColumn(s, idProperty);
		} else {
			dialect.addIdColumn(s, Object.class, 36);
		}
		if (optimisticLocking) {
			s.append(",\n version INTEGER DEFAULT 0");
		}
	}
	
	@Override
	protected void addPrimaryKey(SqlDialect dialect, StringBuilder s) {
		dialect.addPrimaryKey(s, "id");
	}	
}
