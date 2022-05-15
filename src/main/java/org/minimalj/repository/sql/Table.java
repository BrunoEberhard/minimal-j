package org.minimalj.repository.sql;

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
import java.util.Objects;
import java.util.UUID;

import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.Keys.MethodProperty;
import org.minimalj.model.annotation.AutoIncrement;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.list.RelationCriteria;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

@SuppressWarnings("rawtypes")
public class Table<T> extends AbstractTable<T> {
	
	protected final PropertyInterface idProperty;
	protected final boolean optimisticLocking;
	protected final boolean autoIncrement;

	protected final String selectAllQuery;

	protected final HashMap<PropertyInterface, DependableTable> dependables;
	protected final HashMap<PropertyInterface, ListTable> lists;
	
	public Table(SqlRepository sqlRepository, Class<T> clazz) {
		this(sqlRepository, null, clazz);
	}
	
	public Table(SqlRepository sqlRepository, String name, Class<T> clazz) {
		super(sqlRepository, name, clazz);

		this.idProperty = Objects.requireNonNull(FlatProperties.getProperty(clazz, "id", true));
		this.autoIncrement = idProperty != null && isAutoIncrement(idProperty);

		this.optimisticLocking = FieldUtils.hasValidVersionfield(clazz);

		List<PropertyInterface> lists = FlatProperties.getListProperties(clazz);
		this.lists = createListTables(lists);
		this.dependables = createDependableTables();
		
		this.selectAllQuery = selectAllQuery();
	}
	
	static boolean isAutoIncrement(PropertyInterface idProperty) {
		AutoIncrement autoIncrement = idProperty.getAnnotation(AutoIncrement.class);
		if (autoIncrement != null) {
			return autoIncrement.value();
		} else {
			Class<?> idClass = idProperty.getClazz();
			return idClass == Integer.class || idClass == Long.class;
		}
	}
	
	@Override
	public void createTable(SqlDialect dialect) {
		super.createTable(dialect);
		for (Object object : dependables.values()) {
			DependableTable dependableTable = (DependableTable) object;
			dependableTable.createTable(dialect);
		}
		for (Object object : lists.values()) {
			AbstractTable subTable = (AbstractTable) object;
			subTable.createTable(dialect);
		}
	}

	@Override
	protected void dropTable(SqlDialect dialect) {
		for (Object object : dependables.values()) {
			DependableTable dependableTable = (DependableTable) object;
			dependableTable.dropTable(dialect);
		}
		for (Object object : lists.values()) {
			AbstractTable subTable = (AbstractTable) object;
			subTable.dropTable(dialect);
		}
		super.dropTable(dialect);
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
	
	@Override
	public void clear() {
		for (Object object : lists.values()) {
			AbstractTable subTable = (AbstractTable) object;
			subTable.clear();
		}
		super.clear();
	}
	
	protected UUID createId() {
		return UUID.randomUUID();
	}
	
	public Object insert(T object) {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, true)) {
			return insert(object, insertStatement);
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't insert in " + getTableName() + " with " + object);
		}
	}

	public void insert(List<T> objects) {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, true)) {
			for (T object : objects) {
				insert(object, insertStatement);
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't bulk insert in " + getTableName());
		}
	}

	private Object insert(T object, PreparedStatement insertStatement) throws SQLException {
		Object id;
		if (idProperty != null) {
			id = idProperty.getValue(object);
			if (id == null && !autoIncrement) {
				id = createId();
				idProperty.setValue(object, id);
				id = id.toString();
			}
		} else {
			id = createId();
			id = id.toString();
		}
		setParameters(insertStatement, object, autoIncrement ? ParameterMode.INSERT_AUTO_INCREMENT : ParameterMode.INSERT, id);
		insertStatement.execute();
		if (id == null || autoIncrement) {
			try (ResultSet rs = insertStatement.getGeneratedKeys()) {
				rs.next();
				id = rs.getObject(1);
				idProperty.setValue(object, id);
			}
		}
		insertDependables(id, object);
		insertLists(object);
		if (object instanceof Code) {
			sqlRepository.invalidateCodeCache(object.getClass());
		}
		return id;
	}

	protected void insertDependables(Object objectId, T object) {
		for (Entry<PropertyInterface, DependableTable> entry : dependables.entrySet()) {
			Object value = entry.getKey().getValue(object);
			if (value != null) {
				entry.getValue().insert(objectId, value);
			}
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

	public void delete(Object object) {
		Object id = IdUtils.getId(object);
		deleteById(id);
	}

	public void deleteById(Object id) {
		deleteDependables(sqlRepository.read(clazz, id));
		deleteLists(sqlRepository.read(clazz, id));
		try (PreparedStatement updateStatement = createStatement(sqlRepository.getConnection(), deleteQuery, false)) {
			updateStatement.setObject(1, id);
			updateStatement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't delete " + getTableName() + " with ID " + id);
		}
	}

	protected void deleteDependables(Object id) {
		for (DependableTable dependableTable : dependables.values()) {
			dependableTable.delete(id);
		}
	}
	
	protected void deleteLists(T object) {
		for (ListTable list : lists.values()) {
			// TODO implement this more efficient
			list.replaceList(object, Collections.emptyList());
		}
	}
	
	public int delete(Class<?> clazz, Criteria criteria) {
		WhereClause<T> whereClause = new WhereClause<>(this, criteria);
		if (!lists.isEmpty()) {
			// TODO implement this more efficient
			int count = 0;
			String deleteString = "SELECT id FROM " + getTableName() + whereClause.getClause();
			try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), deleteString, false)) {
				for (int i = 0; i < whereClause.getValueCount(); i++) {
					sqlRepository.getSqlDialect().setParameter(statement, i + 1, whereClause.getValue(i));
				}
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						Object id = resultSet.getObject(1);
						deleteById(id);
						count++;
					}
					return count;
				}
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "read with Criteria failed");
			}
		} else {
			String deleteString = "DELETE FROM " + getTableName() + whereClause.getClause();
			try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), deleteString, false)) {
				for (int i = 0; i < whereClause.getValueCount(); i++) {
					sqlRepository.getSqlDialect().setParameter(statement, i + 1, whereClause.getValue(i));
				}
				statement.execute();
				return statement.getUpdateCount();
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "delete with Criteria failed");
			}
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

	protected ListTable createListTable(PropertyInterface property) {
		Class<?> elementClass = property.getGenericClass();
		String subTableName = buildSubTableName(property);
		if (IdUtils.hasId(elementClass)) {
			return new CrossTable<>(sqlRepository, subTableName, elementClass, idProperty);
		} else {
			return new SubTable(sqlRepository, subTableName, elementClass, idProperty);
		}
	}
	
	private LinkedHashMap<PropertyInterface, DependableTable> createDependableTables() {
		LinkedHashMap<PropertyInterface, DependableTable> dependables = new LinkedHashMap<>();
		for (PropertyInterface property : FlatProperties.getProperties(clazz).values()) {
			Class<?> fieldClazz = property.getClazz();
			if (isDependable(property) && fieldClazz != clazz) {
				String tableName = buildSubTableName(property);
				DependableTable dependableTable = new DependableTable(sqlRepository, tableName, property.getClazz(), idProperty);
				dependables.put(property, dependableTable);
			}
		}
		return dependables;
	}
	
	public DependableTable getDependableTable(PropertyInterface property) {
		return dependables.get(property);
	}	
	
	protected String buildSubTableName(PropertyInterface property) {
		return getTableName() + "__" + property.getName();
	}
	
	public void update(T object) {
		updateWithId(object, IdUtils.getId(object));
	}
	
	void updateWithId(T object, Object id) {
		try (PreparedStatement updateStatement = createStatement(sqlRepository.getConnection(), updateQuery, false)) {
			int parameterIndex = setParameters(updateStatement, object, ParameterMode.UPDATE, id);
			if (optimisticLocking) {
				updateStatement.setInt(parameterIndex, IdUtils.getVersion(object));
				updateStatement.execute();
				if (updateStatement.getUpdateCount() == 0) {
					throw new IllegalStateException("Optimistic locking failed");
				}
			} else {
				updateStatement.execute();
			}
			
			updateDependables(object, id);
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
	
	protected void updateDependables(T object, Object objectId) {
		for (Entry<PropertyInterface, DependableTable> entry : dependables.entrySet()) {
			entry.getValue().delete(objectId);
			Object value = entry.getKey().getValue(object);
			if (value != null) {
				entry.getValue().insert(objectId, value);
			}
		}
	}

	public T read(Object id) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, id);
			T object = executeSelect(selectByIdStatement);
			if (object != null) {
				loadDependables(id, object);
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
	
	protected List<String> getColumns(Object[] keys) {
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
	
	public long count(Query query) {
		query = getCriteria(query);
		if (query instanceof RelationCriteria) {
			RelationCriteria relationCriteria = (RelationCriteria) query;
			String queryString = "SELECT COUNT(*) FROM " + relationCriteria.getCrossName() + " WHERE id = ?";
			try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), queryString, false)) {
				sqlRepository.getSqlDialect().setParameter(statement, 1, relationCriteria.getRelatedId());
				return executeSelectCount(statement);
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "count failed");
			}
		} else {
			WhereClause<T> whereClause = new WhereClause<>(this, query);
			String queryString = "SELECT COUNT(*) FROM " + getTableName() + whereClause.getClause();
			try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), queryString, false)) {
				for (int i = 0; i < whereClause.getValueCount(); i++) {
					sqlRepository.getSqlDialect().setParameter(statement, i + 1, whereClause.getValue(i));
				}
				return executeSelectCount(statement);
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "count failed");
			}
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
		WhereClause<T> whereClause = new WhereClause<>(this, query);
		String select = getCriteria(query) instanceof RelationCriteria ? select(resultClass, (RelationCriteria) getCriteria(query)) : select(resultClass);
		String queryString = select + whereClause.getClause();
		try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), queryString, false)) {
			for (int i = 0; i < whereClause.getValueCount(); i++) {
				sqlRepository.getSqlDialect().setParameter(statement, i + 1, whereClause.getValue(i));
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
	
	protected void loadDependables(Object id, T object) throws SQLException {
		for (Entry<PropertyInterface, DependableTable> dependableTableEntry : dependables.entrySet()) {
			Object value = dependableTableEntry.getValue().read(id);
			PropertyInterface dependableProperty = dependableTableEntry.getKey();
			dependableProperty.setValue(object, value);
		}
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
        return "SELECT * FROM " + getTableName() + " WHERE id = ?";
	}
	
	protected String selectAllQuery() {
        return "SELECT * FROM " + getTableName();
	}
	
	@Override
	protected String insertQuery() {
		PropertyInterface idProperty = FlatProperties.getProperty(clazz, "id", true);
		// cannot use field autoIncrement because this method is called by the super constructor before initialization
		boolean autoIncrementId = idProperty != null && isAutoIncrement(idProperty);

		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO ").append(getTableName()).append(" (");
		for (String columnName : getColumns().keySet()) {
			s.append(columnName).append(", ");
		}
		if (autoIncrementId) {
			s.delete(s.length() - 2, s.length());
		} else {
			s.append("id");
		}
		s.append(") VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		if (autoIncrementId) {
			s.delete(s.length() - 2, s.length());
		} else {
			s.append("?");
		}
		s.append(")");

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
        return "DELETE FROM " + getTableName() + " WHERE id = ?";
	}
	
	@Override
	protected void addSpecialColumns(SqlDialect dialect, StringBuilder s) {
		dialect.addIdColumn(s, idProperty);
		if (optimisticLocking) {
			s.append(",\n version INTEGER DEFAULT 0");
		}
	}
	
	@Override
	protected void addPrimaryKey(SqlDialect dialect, StringBuilder s) {
		dialect.addPrimaryKey(s, "id");
	}
}
