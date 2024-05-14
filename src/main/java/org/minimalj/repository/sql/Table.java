package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.minimalj.model.Code;
import org.minimalj.model.Dependable;
import org.minimalj.model.Keys;
import org.minimalj.model.Keys.MethodProperty;
import org.minimalj.model.annotation.AutoIncrement;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Property;
import org.minimalj.repository.list.RelationCriteria;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Order;
import org.minimalj.repository.query.Query;
import org.minimalj.util.Codes;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

@SuppressWarnings("rawtypes")
public class Table<T> extends AbstractTable<T> {
	
	protected final Property idProperty;
	protected final boolean optimisticLocking;
	protected final boolean autoIncrement;

	protected final String selectAllQuery;

	protected final HashMap<Property, DependableTable> dependables;
	protected final HashMap<Property, ListTable> lists;
	
	public Table(SqlRepository sqlRepository, Class<T> clazz) {
		this(sqlRepository, null, clazz);
	}
	
	public Table(SqlRepository sqlRepository, String name, Class<T> clazz) {
		super(sqlRepository, name, clazz);

		this.idProperty = Objects.requireNonNull(FlatProperties.getProperty(clazz, "id", true));
		this.autoIncrement = idProperty != null && isAutoIncrement(idProperty);

		this.optimisticLocking = FieldUtils.hasValidVersionfield(clazz);

		List<Property> lists = FlatProperties.getListProperties(clazz);
		this.lists = createListTables(lists);
		this.dependables = createDependableTables();
		
		this.selectAllQuery = selectAllQuery();
	}
	
	static boolean isAutoIncrement(Property idProperty) {
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
			if (!Dependable.class.isAssignableFrom(subTable.clazz)) {
				subTable.createTable(dialect);
			}
		}
	}

	@Override
	public void collectEnums(Set<Class<? extends Enum<?>>> enms) {
		super.collectEnums(enms);
		for (Object object : dependables.values()) {
			DependableTable dependableTable = (DependableTable) object;
			dependableTable.collectEnums(enms);
		}
		for (Object object : lists.values()) {
			AbstractTable subTable = (AbstractTable) object;
			subTable.collectEnums(enms);
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
			if (!Dependable.class.isAssignableFrom(subTable.clazz)) {
				subTable.dropTable(dialect);
			}
		}
		super.dropTable(dialect);
	}

	@Override
	public void createIndexes(SqlDialect dialect) {
		super.createIndexes(dialect);
		for (Object object : lists.values()) {
			AbstractTable subTable = (AbstractTable) object;
			if (!Dependable.class.isAssignableFrom(subTable.clazz)) {
				subTable.createIndexes(dialect);
			}
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
			Codes.invalidateCodeCache(object.getClass());
		}
		return id;
	}

	protected void insertDependables(Object objectId, T object) {
		for (Entry<Property, DependableTable> entry : dependables.entrySet()) {
			Object value = entry.getKey().getValue(object);
			if (value != null) {
				entry.getValue().insert(objectId, value, null);
			}
		}
	}
	
	protected void insertLists(T object) {
		for (Entry<Property, ListTable> listEntry : lists.entrySet()) {
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
		deleteDependables(id);
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
			dependableTable.delete(id, null);
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

	private LinkedHashMap<Property, ListTable> createListTables(List<Property> listProperties) {
		LinkedHashMap<Property, ListTable> lists = new LinkedHashMap<>();
		for (Property listProperty : listProperties) {
			ListTable listTable = createListTable(listProperty);
			lists.put(listProperty, listTable);
		}
		return lists;
	}

	protected ListTable createListTable(Property property) {
		Class<?> elementClass = property.getGenericClass();
		String subTableName = buildSubTableName(property);
		if (IdUtils.hasId(elementClass)) {
			if (!Dependable.class.isAssignableFrom(elementClass)) {
				return new CrossTable<>(sqlRepository, subTableName, elementClass, idProperty);
			} else {
				return new ContainedSubTable<>(sqlRepository, (Class<? extends Dependable>) elementClass);
			}
		} else {
			return new SubTable<>(sqlRepository, subTableName, elementClass, idProperty);
		}
	}
	
	private LinkedHashMap<Property, DependableTable> createDependableTables() {
		LinkedHashMap<Property, DependableTable> dependables = new LinkedHashMap<>();
		for (Property property : FlatProperties.getProperties(clazz).values()) {
			Class<?> fieldClazz = property.getClazz();
			if (isDependable(property) && fieldClazz != clazz) {
				String tableName = buildSubTableName(property);
				DependableTable dependableTable = createDependableTable(property, tableName);
				dependables.put(property, dependableTable);
			}
		}
		return dependables;
	}

	protected DependableTable createDependableTable(Property property, String tableName) {
		return new DependableTable(sqlRepository, tableName, property.getClazz(), idProperty);
	}
	
	public DependableTable getDependableTable(Property property) {
		return dependables.get(property);
	}	
	
	Collection<DependableTable> getDependableTables() {
		return dependables.values();
	}

	Collection<ListTable> getListTables() {
		return lists.values();
	}

	protected String buildSubTableName(Property property) {
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
			
			updateDependables(object, id, null);
			for (Entry<Property, ListTable> listTableEntry : lists.entrySet()) {
				List list  = (List) listTableEntry.getKey().getValue(object);
				listTableEntry.getValue().replaceList(object, list);
			}
			
			if (object instanceof Code) {
				Codes.invalidateCodeCache(object.getClass());
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
		}
	}
	
	protected void updateDependables(T object, Object objectId, Integer time) {
		for (Entry<Property, DependableTable> entry : dependables.entrySet()) {
			Object value = entry.getKey().getValue(object);
			entry.getValue().update(objectId, value, time);
		}
	}

	public T read(Object id) {
		Map<Class<?>, Map<Object, Object>> loadedReferences = new HashMap<>();
		return read(id, loadedReferences);
	}

	public T read(Object id, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, id);
			T object = executeSelect(selectByIdStatement, loadedReferences);
			if (loadedReferences != SqlRepository.DONT_LOAD_REFERENCES) {
				Map<Object, Object> loadedReferencesOfClass = loadedReferences.computeIfAbsent(clazz, c -> new HashMap<>());
				loadedReferencesOfClass.put(id, object);
			}
			if (object != null) {
				loadDependables(id, object, null, loadedReferences);
				loadLists(object, loadedReferences);
			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id);
		}
	}
	
	/**
	 * <i>warning:</i> project specific. Could change or be removed.
	 * 
	 * @param id id of entity
	 * @return entity with loaded dependables but without loading lists of entities with ids
	 */
	public T readDontLoadReferences(Object id) {
		return read(id, SqlRepository.DONT_LOAD_REFERENCES);
	}
	
	protected List<String> getColumns(Object[] keys) {
		List<String> result = new ArrayList<>();
		Property[] properties = Keys.getProperties(keys);
		for (Property p : properties) {
			if (p instanceof MethodProperty) {
				throw new IllegalArgumentException("Not possible to query for method properties");
			} else if (p.getPath().equals("id")) {
				result.add("id");
				continue;
			}
			for (Map.Entry<String, Property> entry : columns.entrySet()) {
				Property property = entry.getValue();
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
	
	public <S> List<S> find(Query query, Class<S> resultClass, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		WhereClause<T> whereClause = new WhereClause<>(this, query);
		String select = getCriteria(query) instanceof RelationCriteria ? select(resultClass, (RelationCriteria) getCriteria(query)) : select(resultClass);
		String queryString = select + whereClause.getClause();
		try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), queryString, false)) {
			for (int i = 0; i < whereClause.getValueCount(); i++) {
				sqlRepository.getSqlDialect().setParameter(statement, i + 1, whereClause.getValue(i));
			}
			return resultClass == getClazz() ? (List<S>) executeSelectAll(statement, loadedReferences) : executeSelectViewAll(resultClass, statement);
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
			Map<String, Property> propertiesByColumns = sqlRepository.findColumns(resultClass);
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
			Map<String, Property> propertiesByColumns = sqlRepository.findColumns(resultClass);
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
				loadViewLists(resultObject, loadedReferences);
				loadViewDependables(resultObject, loadedReferences);
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
				loadViewLists(result, loadedReferences);	
				loadViewDependables(result, loadedReferences);
			}
		}
		return result;
	}
	
	protected void loadDependables(Object id, T object, Integer time, Map<Class<?>, Map<Object, Object>> loadedReferences) throws SQLException {
		for (Entry<Property, DependableTable> dependableTableEntry : dependables.entrySet()) {
			Object value = dependableTableEntry.getValue().read(id, time, loadedReferences);
			Property dependableProperty = dependableTableEntry.getKey();
			dependableProperty.setValue(object, value);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void loadLists(T object, Map<Class<?>, Map<Object, Object>> loadedReferences) throws SQLException {
		for (Entry<Property, ListTable> listTableEntry : lists.entrySet()) {
			if (loadedReferences == SqlRepository.DONT_LOAD_REFERENCES && IdUtils.hasId(listTableEntry.getKey().getGenericClass())) {
				continue;
			}
			List values = listTableEntry.getValue().getList(object, loadedReferences);
			Property listProperty = listTableEntry.getKey();
			listProperty.setValue(object, values);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <S> void loadViewLists(S result, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		List<Property> viewLists = FlatProperties.getListProperties(result.getClass());
		for (Property viewListProperty : viewLists) {
			for (Entry<Property, ListTable> listPropertyEntry : lists.entrySet()) {
				if (viewListProperty.getPath().equals(listPropertyEntry.getKey().getPath())) {
					List values = listPropertyEntry.getValue().getList(result, loadedReferences);
					viewListProperty.setValue(result, values);

					break;
				}
			}
		}
	}

	protected <S> void loadViewDependables(S object, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		Collection<Property> properties = FlatProperties.getProperties(object.getClass()).values();
		for (Entry<Property, DependableTable> dependableTableEntry : dependables.entrySet()) {
			Property dependableProperty = dependableTableEntry.getKey();
			Optional<Property> propertyOptional = properties.stream().filter(p -> p.getPath().equals(dependableProperty.getPath())).findFirst();
			if (propertyOptional.isPresent()) {
				Object value = dependableTableEntry.getValue().read(IdUtils.getId(object), null, loadedReferences);
				propertyOptional.get().setValue(object, value);
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
		Property idProperty = FlatProperties.getProperty(clazz, "id", true);
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
		return updateQuery(FieldUtils.hasValidVersionfield(clazz));
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
