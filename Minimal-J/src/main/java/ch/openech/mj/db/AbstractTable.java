package ch.openech.mj.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.criteria.Criteria.MaxResultsCriteria;
import ch.openech.mj.criteria.Criteria.SimpleCriteria;
import ch.openech.mj.criteria.CriteriaOperator;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.Search;
import ch.openech.mj.model.ViewUtil;
import ch.openech.mj.model.properties.ChainedProperty;
import ch.openech.mj.model.properties.SimpleProperty;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.GenericUtils;
import ch.openech.mj.util.IdUtils;
import ch.openech.mj.util.LoggingRuntimeException;
import ch.openech.mj.util.StringUtils;

/**
 * Minimal-J internal<p>
 *
 * Base class of all table representing classes in this persistence layer.
 * Normally you should not need to extend from this class directly. Use
 * the existing subclasses or only the methods in DbPersistence.
 * 
 */
public abstract class AbstractTable<T> {
	public static final Logger sqlLogger = Logger.getLogger("SQL");
	
	protected final DbPersistence dbPersistence;
	protected final DbPersistenceHelper helper;
	protected final Class<T> clazz;
	protected final LinkedHashMap<String, PropertyInterface> columns;
	protected final LinkedHashMap<String, PropertyInterface> lists;
	
	protected final String name;

	protected final List<Index<T>> indexes = new ArrayList<>();
	protected final Map<Search<T>, MultiIndex<T>> searches = new HashMap<>();
	
	protected final Map<Connection, Map<String, PreparedStatement>> statements = new HashMap<>();

	protected final String selectByIdQuery;
	protected final String insertQuery;
	protected final String selectMaxIdQuery;
	protected final String clearQuery;
	

	public AbstractTable(DbPersistence dbPersistence, String name, Class<T> clazz) {
		this.dbPersistence = dbPersistence;
		this.helper = new DbPersistenceHelper(dbPersistence);
		this.name = name != null ? name : StringUtils.toDbName(clazz.getSimpleName());
		this.clazz = clazz;
		this.columns = findColumns(clazz);
		this.lists = findLists(clazz);
		
		this.selectByIdQuery = selectByIdQuery();
		this.insertQuery = insertQuery();
		this.selectMaxIdQuery = selectMaxIdQuery();
		this.clearQuery = clearQuery();
		
		findImmutables();
		findIndexes();
		findSearches();
	}

	private LinkedHashMap<String, PropertyInterface> findColumns(Class<?> clazz) {
		LinkedHashMap<String, PropertyInterface> columns = new LinkedHashMap<String, PropertyInterface>();

		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || FieldUtils.isTransient(field)) continue;
			String fieldName = StringUtils.toDbName(field.getName());
			if (StringUtils.equals(fieldName, "ID", "VERSION")) continue;
			if (FieldUtils.isList(field)) continue;
			if (FieldUtils.isFinal(field) && !FieldUtils.isSet(field)) {
				Map<String, PropertyInterface> inlinePropertys = findColumns(field.getType());
				boolean hasClassName = FieldUtils.hasClassName(field);
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = fieldName + "_" + inlineKey;
					}
					columns.put(key, new ChainedProperty(clazz, field, inlinePropertys.get(inlineKey)));
				}
			} else {
				columns.put(fieldName, new SimpleProperty(clazz, field));
			}
		}
		return columns;
	}	
	
	private LinkedHashMap<String, PropertyInterface> findLists(Class<?> clazz) {
		LinkedHashMap<String, PropertyInterface> properties = new LinkedHashMap<String, PropertyInterface>();
		
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || FieldUtils.isTransient(field)) continue;
			if (FieldUtils.isFinal(field) && !FieldUtils.isList(field)) {
				// This is needed to check if an inline Property contains a List
				Map<String, PropertyInterface> inlinePropertys = findLists(field.getType());
				boolean hasClassName = FieldUtils.hasClassName(field);
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = field.getName() + StringUtils.upperFirstChar(inlineKey);
					}
					properties.put(key, new ChainedProperty(clazz, field, inlinePropertys.get(inlineKey)));
				}
			} else if (FieldUtils.isList(field)) {
				properties.put(field.getName(), new SimpleProperty(clazz, field));
			}
		}
		return properties; 
	}
	
	protected LinkedHashMap<String, PropertyInterface> getColumns() {
		return columns;
	}

	protected LinkedHashMap<String, PropertyInterface> getLists() {
		return lists;
	}
	
	protected Collection<Index<T>> getIndexes() {
		return indexes;
	}
	
	protected PreparedStatement getStatement(Connection connection, String query, boolean returnGeneratedKeys) throws SQLException {
		if (!statements.containsKey(connection)) {
			statements.put(connection, new HashMap<String, PreparedStatement>());
		}
		Map<String, PreparedStatement> statementsForConnection = statements.get(connection);
		if (!statementsForConnection.containsKey(query)) {
			statementsForConnection.put(query, createStatement(connection, query, returnGeneratedKeys));
		}
		return statementsForConnection.get(query);
	}
	
	PreparedStatement createStatement(Connection connection, String query, boolean returnGeneratedKeys) throws SQLException {
		if (returnGeneratedKeys) {
			if (sqlLogger.isLoggable(Level.FINE)) {
				return new LoggingPreparedStatement(connection, query, Statement.RETURN_GENERATED_KEYS, sqlLogger);
			} else {
				return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			}
		} else {
			if (sqlLogger.isLoggable(Level.FINE)) {
				return new LoggingPreparedStatement(connection, query, sqlLogger);
			} else {
				return connection.prepareStatement(query);
			}
		}
	}

	public int getMaxId() {
		try {
			PreparedStatement statement = getStatement(dbPersistence.getConnection(), selectMaxIdQuery, false);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getInt(1);
				} else {
					return 0;
				}
			} catch (SQLException x) {
				throw new LoggingRuntimeException(x, sqlLogger, "Couldn't get max Id of " + getTableName());
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't get max Id of " + getTableName());
		}
	}
	
	public void create() throws SQLException {
		DbCreator creator = new DbCreator(dbPersistence);
		creator.create(dbPersistence.getConnection(), this);
	}
	
	public void clear() {
		try {
			PreparedStatement statement = getStatement(dbPersistence.getConnection(), clearQuery, false);
			statement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Clear of Table " + getTableName() + " failed");
		}
	}
	
	public List<T> search(Search<T> search, Object query) {
		MultiIndex<T> multiIndex = searches.get(search);
		List<Long> ids = multiIndex.findIds(query);
		List<T> objects = new ArrayList<>(ids.size());
		for (long id : ids) {
			objects.add(multiIndex.lookup(id));
		}
		return objects;
	}

	public List<T> read(Criteria criteria) {
		// TODO implement all the criteria and join of criteria
		if (criteria instanceof SimpleCriteria) {
			SimpleCriteria simpleCriteria = (SimpleCriteria) criteria;
			PropertyInterface propertyInterface = Keys.getProperty(simpleCriteria.getKey());
			String query = "select * from " + getTableName() + " where " + whereStatement(propertyInterface.getFieldPath(), simpleCriteria.getOperator());
			try {
				PreparedStatement statement = getStatement(dbPersistence.getConnection(), query, false);
				Object value = simpleCriteria.getValue();
				if (!(value instanceof Integer || value instanceof Long) && DbPersistenceHelper.isView(propertyInterface)) {
					value = IdUtils.getId(value);
				}
				statement.setObject(1, value);
				return executeSelectAll(statement);
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "read with SimpleCriteria failed");
			}
		} else if (criteria instanceof MaxResultsCriteria) {
			MaxResultsCriteria maxResultsCriteria = (MaxResultsCriteria) criteria;
			String query = "select * from " + getTableName();
			try {
				PreparedStatement statement = getStatement(dbPersistence.getConnection(), query, false);
				return executeSelectAll(statement, maxResultsCriteria.getMaxResults());
			} catch (SQLException e) {
				throw new LoggingRuntimeException(e, sqlLogger, "read with MaxResultsCriteria failed");
			}
		}
		throw new IllegalArgumentException(criteria + " not yet implemented");
	}

	private String whereStatement(final String wholeFieldPath, CriteriaOperator criteriaOperator) {
		String fieldPath = wholeFieldPath;
		String column;
		while (true) {
			column = findColumn(fieldPath);
			if (column != null) break;
			int pos = fieldPath.lastIndexOf('.');
			if (pos < 0) throw new IllegalArgumentException("FieldPath " + wholeFieldPath + " not even partially found in " + getTableName());
			fieldPath = fieldPath.substring(0, pos);
		}
		if (fieldPath.length() < wholeFieldPath.length()) {
			String restOfFieldPath = wholeFieldPath.substring(fieldPath.length() + 1);
			PropertyInterface subProperty = columns.get(column);
			AbstractTable<?> subTable = dbPersistence.getTable(subProperty.getFieldClazz());
			return column + " = select (ID from " + subTable.getTableName() + " where " + subTable.whereStatement(restOfFieldPath, criteriaOperator) + ")";
		} else {
			return column + " " + criteriaOperator.getOperatorAsString() + " ?";
		}
	}
	
	private String findColumn(String fieldPath) {
		for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
			if (entry.getValue().getFieldPath().equals(fieldPath)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	protected String getTableName() {
		return name;
	}
	
	public Class<T> getClazz() {
		return clazz;
	}
	
	private void findImmutables() {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			PropertyInterface property = column.getValue();
			if (DbPersistenceHelper.isView(property)) continue;
			if (DbPersistenceHelper.isReference(property)) {
				AbstractTable<?> refTable = dbPersistence.getImmutableTable(property.getFieldClazz());
				if (refTable == null) {
					dbPersistence.addImmutableClass(property.getFieldClazz());
				}
			}
		}
	}

	private void findSearches() {
		System.out.println("Find Searches in " + clazz.getSimpleName());
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isFinal(field) || !FieldUtils.isStatic(field)) continue;
			if (field.getType() == Search.class) {
				@SuppressWarnings("unchecked")
				Search<T> search = (Search<T>) FieldUtils.getStaticValue(field);
				createFulltextIndex(search);
 			}
		}
	}

	private void findIndexes() {
//		for (Map.Entry<String, PropertyInterface> column : columns.entrySet()) {
//			PropertyInterface property = column.getValue();
//			if (property.getType() instanceof Reference<?>) {
//				createIndex(property, property.getFieldPath());
//			}
//		}
	}
	
	// execution helpers
	
	protected long executeInsertWithAutoIncrement(PreparedStatement statement, T object) throws SQLException {
		return executeInsertWithAutoIncrement(statement, object, null);
	}
	
	protected long executeInsertWithAutoIncrement(PreparedStatement statement, T object, Integer hash) throws SQLException {
		setParameters(statement, object, false, true, hash);
		statement.execute();
		try (ResultSet autoIncrementResultSet = statement.getGeneratedKeys()) {
			autoIncrementResultSet.next();
			long id = autoIncrementResultSet.getLong(1);
			if (sqlLogger.isLoggable(Level.FINE)) sqlLogger.fine("AutoIncrement is " + id);
			return id;
		}
	}
	
	protected void executeInsert(PreparedStatement statement, T object) throws SQLException {
		setParameters(statement, object);
		statement.execute();
	}

	protected T executeSelect(PreparedStatement preparedStatement) throws SQLException {
		return executeSelect(preparedStatement, null);
	}
	
	protected T executeSelect(PreparedStatement preparedStatement, Integer time) throws SQLException {
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				return readResultSetRow(resultSet, time);
			} else {
				return null;
			}
		}
	}

	protected List<T> executeSelectAll(PreparedStatement preparedStatement) throws SQLException {
		return executeSelectAll(preparedStatement, Long.MAX_VALUE);
	}
	
	private List<T> executeSelectAll(PreparedStatement preparedStatement, long maxResults) throws SQLException {
		List<T> result = new ArrayList<T>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next() && result.size() < maxResults) {
				T object = readResultSetRow(resultSet, null);
				result.add(object);
			}
		}
		return result;
	}
	
	
	protected T readResultSetRow(ResultSet resultSet, Integer time) throws SQLException {
		T result = CloneHelper.newInstance(clazz);
		
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			String columnName = resultSet.getMetaData().getColumnName(columnIndex);
			if ("ID".equalsIgnoreCase(columnName) && this instanceof Table) {
				IdUtils.setId(result, resultSet.getLong(columnIndex));
				continue;
			} else if ("VERSION".equalsIgnoreCase(columnName) && this instanceof HistorizedTable) {
				IdUtils.setVersion(result, resultSet.getInt(columnIndex));
				continue;
			}
			
			PropertyInterface property = columns.get(columnName);
			if (property == null) continue;
			
			Object value = resultSet.getObject(columnIndex);
			if (value != null) {
				Class<?> fieldClass = property.getFieldClazz();
				if (DbPersistenceHelper.isView(property)) {
					Class<?> viewedClass = DbPersistenceHelper.getViewedClass(property);
					Table<?> referenceTable = (Table<?>) dbPersistence.getTable(viewedClass);
					Object referenceObject = referenceTable.read(((Number) value).longValue(), false); // false -> subEntities not loaded
					
					value = CloneHelper.newInstance(fieldClass);
					ViewUtil.view(referenceObject, value);
				} else if (DbPersistenceHelper.isReference(property)) {
					value = dereference(fieldClass, IdUtils.convertToLong(value), time);
				} else if (fieldClass == Set.class) {
					Set<?> set = (Set<?>) property.getValue(result);
					Class<?> enumClass = GenericUtils.getGenericClass(property.getType());
					EnumUtils.fillSet((int) value, enumClass, set);
					continue; // skip setValue, it's final
				} else {
					value = helper.convertToFieldClass(fieldClass, value);
				}
				property.setValue(result, value);
			}
		}
		return result;
	}
	
	protected <D> Object dereference(Class<D> clazz, long id, Integer time) {
		AbstractTable<D> table = dbPersistence.getTable(clazz);
		if (table instanceof ImmutableTable) {
			return ((ImmutableTable<?>) table).read(id);
		} else {
			throw new IllegalArgumentException("Clazz: " + clazz);
		}
	}

	/**
	 * Search or create an immutable.<br>
	 * At the moment no references of other values than immutables are allowed.
	 * 
	 * @param value the object from which to get the reference.
	 * @param insertIfNotExisting true => create if not existing
	 * @return <code>if value not found and parameter insert is false
	 * @throws SQLException
	 */
	private <D> Long getIdOfImmutable(D value, boolean insertIfNotExisting) throws SQLException {
		@SuppressWarnings("unchecked")
		Class<D> clazz = (Class<D>) value.getClass();
		AbstractTable<D> abstractTable = dbPersistence.getTable(clazz);
		if (abstractTable == null) {
			throw new IllegalArgumentException(clazz.getName());
		}
		if (abstractTable instanceof ImmutableTable) {
			return ((ImmutableTable<D>) abstractTable).getOrCreateId(value);
		} else {
			throw new IllegalArgumentException(clazz.getName());
		}
	}
	
	protected int setParameters(PreparedStatement statement, T object) throws SQLException {
		return setParameters(statement, object, null);
	}

	protected int setParameters(PreparedStatement statement, T object, Integer hash) throws SQLException {
		return setParameters(statement, object, false, false, hash);
	}

	protected int setParameters(PreparedStatement statement, T object, boolean doubleValues, boolean insert) throws SQLException {
		return setParameters(statement, object, doubleValues, insert, null);
	}
	
	protected int setParameters(PreparedStatement statement, T object, boolean doubleValues, boolean insert, Integer hash) throws SQLException {
		int parameterPos = 1;
		for (Map.Entry<String, PropertyInterface> column : columns.entrySet()) {
			PropertyInterface property = column.getValue();
			Object value = property.getValue(object);
			if (value != null) {
				if (DbPersistenceHelper.isView(property)) {
					value = IdUtils.getId(value);
				} else if (DbPersistenceHelper.isReference(property)) {
					value = getIdOfImmutable(value, insert);
				} 
			}
			helper.setParameter(statement, parameterPos++, value, property);
			if (doubleValues) helper.setParameter(statement, parameterPos++, value, property);
		}
		if (hash != null) {
			statement.setInt(parameterPos++, hash);
			if (doubleValues) statement.setInt(parameterPos++, hash);
		}
		return parameterPos;
	}
			
	protected abstract String insertQuery();

	protected abstract String selectByIdQuery();

	protected String selectMaxIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT MAX(id) FROM "); query.append(getTableName()); 
		return query.toString();
	}
	
	protected String clearQuery() {
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM "); query.append(getTableName()); 
		return query.toString();
	}
	
	protected String selectIdQuery() {
		StringBuilder where = new StringBuilder();
	
		boolean first = true;	
		
		for (String key : columns.keySet()) {

			if (!first) where.append(" AND "); else first = false;
			
			// where.append(column.getName()); where.append(" = ?");
			// doesnt work for null so pattern is:
			// ((? IS NULL AND col1 IS NULL) OR col1 = ?)
			where.append("((? IS NULL AND "); where.append(key); where.append(" IS NULL) OR ");
			where.append(key); where.append(" = ?)");
		}
		
		if (this instanceof Table) {
			where.append(" AND ((? IS NULL) OR event <= ?)");
			where.append(" AND (endEvent IS NULL OR (endEvent IS NOT NULL AND (? IS NULL OR ? < endEvent)))");
		}
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM "); query.append(getTableName()); query.append(" WHERE ");
		query.append(where);
		
		return query.toString();
	}

	private void createFulltextIndex(Search<T> search) {
		Object[] keys = search.getKeys();
		ColumnIndex<T>[] indexes = new ColumnIndex[keys.length];
		for (int i = 0; i<keys.length; i++) {
			PropertyInterface property = Keys.getProperty(keys[i]);
			if (property.getFieldClazz() == String.class) {
				indexes[i] = createFulltextIndex(keys[i]);
			} else {
				indexes[i] = createIndex(keys[i]);
			}
		}
		MultiIndex<T> index = new MultiIndex<T>(indexes);
		searches.put(search, index);
	}
	
	private ColumnIndex<T> createFulltextIndex(Object key) {
		PropertyInterface property = Keys.getProperty(key);
		String fieldPath = property.getFieldPath();
		ColumnIndex<T> index = createFulltextIndex(property, fieldPath);
		return index;
	}
	
	private ColumnIndex<T> createFulltextIndex(PropertyInterface property, String fieldPath) {
		ColumnIndex<T> result;
		Map.Entry<String, PropertyInterface> entry = findX(fieldPath);

		String myFieldPath = entry.getValue().getFieldPath();
		if (fieldPath.length() > myFieldPath.length()) {
			String rest = fieldPath.substring(myFieldPath.length() + 1);
			AbstractTable<?> innerTable = dbPersistence.getTable(entry.getValue().getFieldClazz());
			ColumnIndex<?> innerIndex = innerTable.createFulltextIndex(property, rest);

			result = new ColumnIndex<T>(dbPersistence, this, property, entry.getKey(), innerIndex);
		} else {
			result = new FulltextIndex<T>(dbPersistence, this, property, entry.getKey());
		}
		indexes.add(result);
		return result;
	}
	
	//

	public ColumnIndex<T> createIndex(Object key) {
		PropertyInterface property = Keys.getProperty(key);
		String fieldPath = property.getFieldPath();
		ColumnIndex<T> index = createIndex(property, fieldPath);
		return index;
	}
	
	public ColumnIndex<T> createIndex(PropertyInterface property, String fieldPath) {
		Map.Entry<String, PropertyInterface> entry = findX(fieldPath);
		if (indexes.contains(entry.getKey())) {
			return (ColumnIndex<T>) indexes.get(indexes.indexOf(entry.getKey()));
		}
		
		ColumnIndex<?> innerIndex = null;
		String myFieldPath = entry.getValue().getFieldPath();
		if (fieldPath.length() > myFieldPath.length()) {
			String rest = fieldPath.substring(myFieldPath.length() + 1);
			AbstractTable<?> innerTable = dbPersistence.getTable(entry.getValue().getFieldClazz());
			innerIndex = innerTable.createIndex(property, rest);
		}
		ColumnIndex<T> result = new ColumnIndex<T>(dbPersistence, this, property, entry.getKey(), innerIndex);
		indexes.add(result);
		return result;
	}
	
	//
	
	protected Entry<String, PropertyInterface> findX(String fieldPath) {
		while (true) {
			for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
				String columnFieldPath = entry.getValue().getFieldPath();
				if (columnFieldPath.equals(fieldPath)) {
					return entry;
				}
			}
			int index = fieldPath.lastIndexOf('.');
			if (index < 0) throw new IllegalArgumentException();
			fieldPath = fieldPath.substring(0, index);
		}
	}

}
