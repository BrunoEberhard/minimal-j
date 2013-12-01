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

import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.properties.ChainedProperty;
import ch.openech.mj.model.properties.FinalReferenceProperty;
import ch.openech.mj.model.properties.SimpleProperty;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.GenericUtils;
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

	protected final Map<Object, Index<T>> indexByKey = new HashMap<>();
	protected final List<Index<T>> indexes = new ArrayList<>();
	
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
	}

	private LinkedHashMap<String, PropertyInterface> findColumns(Class<?> clazz) {
		LinkedHashMap<String, PropertyInterface> columns = new LinkedHashMap<String, PropertyInterface>();

		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || FieldUtils.isTransient(field)) continue;
			String fieldName = StringUtils.toDbName(field.getName());
			if (fieldName.equals("ID")) continue;
			if (FieldUtils.isList(field)) continue;
			if (FieldUtils.isFinal(field) && !FieldUtils.isSet(field)) {
				if (!dbPersistence.isImmutable(field.getType())) {
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
					columns.put(fieldName, new FinalReferenceProperty(clazz, field));
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
			if (!dbPersistence.isImmutable(field.getType()) && FieldUtils.isFinal(field) && !FieldUtils.isList(field)) {
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
	
	public Index<T> getIndex(Object key) {
		return indexByKey.get(key);
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
	
	private PreparedStatement createStatement(Connection connection, String query, boolean returnGeneratedKeys) throws SQLException {
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

	public int getMaxId(Connection connection) {
		try {
			PreparedStatement statement = getStatement(connection, selectMaxIdQuery, false);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getInt(1);
				} else {
					return 0;
				}
			} catch (SQLException x) {
				sqlLogger.log(Level.SEVERE, "Couldn't get max Id of " + getTableName(), x);
				throw new RuntimeException("Couldn't get max Id of " + getTableName());
			}
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Couldn't get max Id of " + getTableName(), x);
			throw new RuntimeException("Couldn't get max Id of " + getTableName());
		}
	}
	
	public void create(Connection connection) throws SQLException {
		DbCreator creator = new DbCreator(dbPersistence);
		creator.create(connection, this);
	}
	
	public void clear(Connection connection) {
		try {
			PreparedStatement statement = getStatement(connection, clearQuery, false);
			statement.execute(clearQuery());
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Clear of Table " + getTableName() + " failed", x);
			throw new RuntimeException("Clear of Table " + getTableName() + " failed");
		}
	}
	
	protected String getTableName() {
		return name;
	}
	
	public Class<T> getClazz() {
		return clazz;
	}
	
	private void findImmutables() {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			if ("ID".equals(column.getKey())) continue;
			PropertyInterface property = column.getValue();
			if (DbPersistenceHelper.isReference(property)) {
				AbstractTable<?> refTable = dbPersistence.getTable(property.getFieldClazz());
				if (refTable == null) {
					if (property.getFieldClazz().equals(List.class)) {
						throw new IllegalArgumentException("Table: " + getTableName());
					}
					refTable = dbPersistence.addImmutableClass(property.getFieldClazz());
				}
			}
		}
	}

	// execution helpers
	
	protected int executeInsertWithAutoIncrement(PreparedStatement statement, T object) throws SQLException {
		return executeInsertWithAutoIncrement(statement, object, null);
	}
	
	protected int executeInsertWithAutoIncrement(PreparedStatement statement, T object, Integer hash) throws SQLException {
		setParameters(statement, object, false, true, hash);
		statement.execute();
		try (ResultSet autoIncrementResultSet = statement.getGeneratedKeys()) {
			autoIncrementResultSet.next();
			int id = autoIncrementResultSet.getInt(1);
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
				return readResultSetRow(resultSet, time).object;
			} else {
				return null;
			}
		}
	}

	protected List<T> executeSelectAll(PreparedStatement preparedStatement) throws SQLException {
		List<T> result = new ArrayList<T>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				T object = readResultSetRow(resultSet, null).object;
				result.add(object);
			}
		}
		return result;
	}
	
	/**
	 * Internal helper class. Needed by readResultSetRow. Allows the returning of
	 * both the object and the id.
	 */
	protected static class ObjectWithId<S> {
		public Integer id;
		public S object;
	}
	
	protected ObjectWithId<T> readResultSetRow(ResultSet resultSet, Integer time) throws SQLException {
		Connection connection = resultSet.getStatement().getConnection();
		ObjectWithId<T> result = new ObjectWithId<>();
		result.object = CloneHelper.newInstance(clazz);
		
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			Object value = resultSet.getObject(columnIndex);
			String columnName = resultSet.getMetaData().getColumnName(columnIndex);
			boolean isId = columnName.equalsIgnoreCase("id");
			if (isId) result.id = (Integer) value;
			PropertyInterface property = columns.get(columnName);
			if (property == null) continue;
			if (isId) {
				property.setValue(result.object, value);
				continue;
			}
			
			if (value != null) {
				Class<?> fieldClass = property.getFieldClazz();
				if (DbPersistenceHelper.isReference(property)) {
					value = dereference(connection, fieldClass, (Integer) value, time);
				} else if (Set.class == fieldClass) {
					Set<?> set = (Set<?>) property.getValue(result.object);
					Class<?> enumClass = GenericUtils.getGenericClass(property.getType());
					EnumUtils.fillSet((int) value, enumClass, set);
					continue; // skip setValue, it's final
				} else {
					value = helper.convertToFieldClass(fieldClass, value);
				}
				property.setValue(result.object, value);
			}
		}
		return result;
	}
	
	protected <D> Object dereference(Connection connection, Class<D> clazz, int id, Integer time) {
		AbstractTable<D> table = dbPersistence.getTable(clazz);
		if (table instanceof ImmutableTable) {
			return ((ImmutableTable<?>) table).read(connection,id);
		} else if (table instanceof HistorizedTable<?>) {
			return ((HistorizedTable<?>) table).read(connection,id, time);			
		} else if (table instanceof Table) {
			return ((Table<?>) table).read(connection,id);
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
	private <D> Integer lookupReference(Connection connection, D value, boolean insertIfNotExisting) throws SQLException {
		@SuppressWarnings("unchecked")
		Class<D> clazz = (Class<D>) value.getClass();
		AbstractTable<D> abstractTable = dbPersistence.getTable(clazz);
		if (abstractTable == null) {
			throw new IllegalArgumentException(clazz.getName());
		}
		if (abstractTable instanceof ImmutableTable) {
			return ((ImmutableTable<D>) abstractTable).getOrCreateId(connection, value);
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
		Connection connection = statement.getConnection();
		int parameterPos = 1;
		for (Map.Entry<String, PropertyInterface> column : columns.entrySet()) {
			PropertyInterface property = column.getValue();
			Object value = property.getValue(object);
			if (value != null) {
				if (DbPersistenceHelper.isReference(property)) {
					try {
						value = lookupReference(connection, value, insert);
					} catch (IllegalArgumentException e) {
						sqlLogger.severe(object.getClass().getName() + " / " + property.getFieldName());
						throw e;
					}
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

	public MultiIndex<T> createFulltextIndex(Object... keys) {
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
		indexByKey.put(keys, index);
		return index;
	}
	
	public ColumnIndex<T> createFulltextIndex(Object key) {
		PropertyInterface property = Keys.getProperty(key);
		String fieldPath = property.getFieldPath();
		ColumnIndex<T> index = createFulltextIndex(property, fieldPath);
		indexByKey.put(key, index);
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
		indexByKey.put(key, index);
		return index;
	}
	
	public ColumnIndex<T> createIndex(PropertyInterface property, String fieldPath) {
		Map.Entry<String, PropertyInterface> entry = findX(fieldPath);

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
	
	public ColumnIndexUnqiue<T> createIndexUnique(Object key) {
		PropertyInterface property = Keys.getProperty(key);
		String fieldPath = property.getFieldPath();
		ColumnIndexUnqiue<T> index = createIndexUnique(property, fieldPath);
		indexByKey.put(key, index);
		return index;
	}
	
	public ColumnIndexUnqiue<T> createIndexUnique(PropertyInterface property, String fieldPath) {
		sqlLogger.info("Create index on " + getTableName() + " with: " + fieldPath);
		Map.Entry<String, PropertyInterface> entry = findX(fieldPath);

		ColumnIndex<?> innerIndex = null;
		String myFieldPath = entry.getValue().getFieldPath();
		if (fieldPath.length() > myFieldPath.length()) {
			String rest = fieldPath.substring(myFieldPath.length() + 1);
			AbstractTable<?> innerTable = dbPersistence.getTable(entry.getValue().getFieldClazz());
			innerIndex = innerTable.createIndex(property, rest);
		}
		ColumnIndexUnqiue<T> result = new ColumnIndexUnqiue<T>(dbPersistence, this, property, entry.getKey(), innerIndex);
		indexes.add(result);
		return result;
	}
	
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
