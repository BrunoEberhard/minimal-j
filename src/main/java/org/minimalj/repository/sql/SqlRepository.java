package org.minimalj.repository.sql;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.minimalj.application.Configuration;
import org.minimalj.model.Code;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.Keys.MethodProperty;
import org.minimalj.model.Model;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.Materialized;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.FieldProperty;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.test.ModelTest;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.Repository;
import org.minimalj.repository.TransactionalRepository;
import org.minimalj.repository.list.QueryResultList;
import org.minimalj.repository.list.SortableList;
import org.minimalj.repository.query.AllCriteria;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Limit;
import org.minimalj.repository.query.Query;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.Codes;
import org.minimalj.util.Codes.CodeCacheItem;
import org.minimalj.util.CsvReader;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * The Mapper to a relationale Database
 * 
 */
public class SqlRepository implements TransactionalRepository {
	private static final Logger logger = Logger.getLogger(SqlRepository.class.getName());
	
	protected final SqlDialect sqlDialect;
	
	private final Map<Class<?>, AbstractTable<?>> tables = new LinkedHashMap<Class<?>, AbstractTable<?>>();
	private final Map<String, AbstractTable<?>> tableByName = new HashMap<String, AbstractTable<?>>();
	private final Map<Class<?>, LinkedHashMap<String, PropertyInterface>> columnsForClass = new HashMap<>(200);
	
	private final DataSource dataSource;
	
	private Connection autoCommitConnection;
	private final BlockingDeque<Connection> connectionDeque = new LinkedBlockingDeque<>();
	private final ThreadLocal<Connection> threadLocalTransactionConnection = new ThreadLocal<>();

	private final HashMap<Class<? extends Code>, CodeCacheItem<? extends Code>> codeCache = new HashMap<>();
	
	public SqlRepository(Model model) {
		this(DataSourceFactory.create(), model.getEntityClasses());
	}
	
	public SqlRepository(DataSource dataSource, Class<?>... classes) {
		this.dataSource = DataSourceFactory.create();
		
		Connection connection = getAutoCommitConnection();
		try {
			sqlDialect = findDialect(connection);
			for (Class<?> clazz : classes) {
				addClass(clazz);
			}
			new ModelTest(classes).assertValid();
			if (createTablesOnInitialize(dataSource)) {
				createTables();
				createCodes();
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Could not determine product name of database");
		}
	}

	private SqlDialect findDialect(Connection connection) throws SQLException {
		if (Configuration.available("MjSqlDialect")) {
			return Configuration.getClazz("MjSqlDialect", SqlDialect.class);
		}
		
		String databaseProductName = connection.getMetaData().getDatabaseProductName();
		if (StringUtils.equals(databaseProductName, "MySQL")) {
			return new SqlDialect.MariaSqlDialect();
		} else if (StringUtils.equals(databaseProductName, "PostgreSQL")) {
			return new SqlDialect.PostgresqlDialect();
		} else if (StringUtils.equals(databaseProductName, "Apache Derby")) {
			return new SqlDialect.DerbySqlDialect();
		} else if (StringUtils.equals(databaseProductName, "H2")) {
			return new SqlDialect.H2SqlDialect();
		} else if (StringUtils.equals(databaseProductName, "Oracle")) {
			return new SqlDialect.OracleSqlDialect();				
		} else {
			throw new RuntimeException("Only Oracle, H2, MySQL/MariaDB and Derby DB supported at the moment. ProductName: " + databaseProductName);
		}
	}
	
	private Connection getAutoCommitConnection() {
		try {
			if (autoCommitConnection == null || !autoCommitConnection.isValid(0)) {
				autoCommitConnection = dataSource.getConnection();
				autoCommitConnection.setAutoCommit(true);
			}
			return autoCommitConnection;
		} catch (Exception e) {
			throw new LoggingRuntimeException(e, logger, "Not possible to create autocommit connection");
		}
	}
	
	public SqlDialect getSqlDialect() {
		return sqlDialect;
	}

	@Override
	public void startTransaction(int transactionIsolationLevel) {
		if (isTransactionActive()) return;
		
		Connection transactionConnection = allocateConnection(transactionIsolationLevel);
		threadLocalTransactionConnection.set(transactionConnection);
	}

	@Override
	public void endTransaction(boolean commit) {
		Connection transactionConnection = threadLocalTransactionConnection.get();
		if (transactionConnection == null) return;
		
		try {
			if (commit) {
				transactionConnection.commit();
			} else {
				transactionConnection.rollback();
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Transaction failed");
		}
		
		releaseConnection(transactionConnection);
		threadLocalTransactionConnection.set(null);
	}
	
	private Connection allocateConnection(int transactionIsolationLevel) {
		Connection connection = connectionDeque.poll();
		while (true) {
			boolean valid = false;
			try {
				valid = connection != null && connection.isValid(0);
			} catch (SQLException x) {
				// ignore
			}
			if (valid) {
				return connection;
			}
			try {
				connection = dataSource.getConnection();
				connection.setTransactionIsolation(transactionIsolationLevel);
				connection.setAutoCommit(false);
				return connection;
			} catch (Exception e) {
				// this could happen if there are already too many connections
				e.printStackTrace();

				logger.log(Level.FINE, "Not possible to create additional connection", e);
			}
			// so no connection available and not possible to create one
			// block and wait till a connection is in deque
			try {
				connectionDeque.poll(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.log(Level.FINEST, "poll for connection interrupted", e);
			}
		}
	}
	
	private void releaseConnection(Connection connection) {
		// last in first out in the hope that recent accessed objects are the fastest
		connectionDeque.push(connection);
	}
	
	/**
	 * Use with care. Removes all content of all tables. Should only
	 * be used for JUnit tests.
	 */
	public void clear() {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			table.clear();
		}
	}

	public boolean isTransactionActive() {
		Connection connection = threadLocalTransactionConnection.get();
		return connection != null;
	}
	
	Connection getConnection() {
		Connection connection = threadLocalTransactionConnection.get();
		if (connection != null) {
			return connection;
		} else {
			connection = getAutoCommitConnection();
			return connection;
		}
	}
	
	private boolean createTablesOnInitialize(DataSource dataSource) throws SQLException {
		// If the classes are not in the classpath a 'instanceof' would throw ClassNotFoundError
		if (StringUtils.equals(dataSource.getClass().getName(), "org.apache.derby.jdbc.EmbeddedDataSource")) {
			return "create".equals(((EmbeddedDataSource) dataSource).getCreateDatabase());
		} else if (StringUtils.equals(dataSource.getClass().getName(), "org.h2.jdbcx.JdbcDataSource")) {
			String url = ((JdbcDataSource) dataSource).getUrl();
			if (url.startsWith("jdbc:h2:mem")) {
				return true;
			}
			try (ResultSet tableDescriptions = getConnection().getMetaData().getTables(null, null, null, new String[] {"TABLE"})) {
				return !tableDescriptions.next();
			}
		}
		return false;
	}
	
	@Override
	public <T> T read(Class<T> clazz, Object id) {
		if (View.class.isAssignableFrom(clazz)) {
			@SuppressWarnings("unchecked")
			Table<T> table = (Table<T>) getTable(ViewUtil.getViewedClass(clazz));
			return table.readView(clazz, id, new HashMap<>());
		} else {
			return getTable(clazz).read(id);
		}
	}


	@Override
	public <T> List<T> find(Class<T> resultClass, Query query) {
		if (query instanceof Limit || query instanceof AllCriteria) {
			@SuppressWarnings("unchecked")
			Table<T> table = (Table<T>) getTable(ViewUtil.resolve(resultClass));
			List<T> list = table.find(query, resultClass);
			return (query instanceof AllCriteria) ? new SortableList<T>(list) : list;
		} else {
			return new SqlQueryResultList<>(this, resultClass, query);
		}
	}
	
	private static class SqlQueryResultList<T> extends QueryResultList<T> {
		private static final long serialVersionUID = 1L;

		public SqlQueryResultList(Repository repository, Class<T> clazz, Query query) {
			super(repository, clazz, query);
		}
		
		@Override
		public boolean canSortBy(Object sortKey) {
			PropertyInterface property = Keys.getProperty(sortKey);
			if (property.getClass() != FieldProperty.class) {
				return false;
			}
			
			return property.getClazz() == String.class || Number.class.isAssignableFrom(property.getClazz())
					|| Temporal.class.isAssignableFrom(property.getClazz());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> long count(Class<T> clazz, Criteria criteria) {
		if (View.class.isAssignableFrom(clazz)) {
			clazz = (Class<T>) ViewUtil.getViewedClass(clazz);
		}
		Table<?> table = getTable(clazz);
		return table.count(criteria);
	}

	@Override
	public <T> Object insert(T object) {
		if (object == null) throw new NullPointerException();
		Object originalId = IdUtils.getId(object);
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		// all repositories should behave to same and not save the new id in the
		// original object.
		IdUtils.setId(object, originalId);
		return table.insert(object);
	}

	@Override
	public <T> void update(T object) {
		if (object == null) throw new NullPointerException();
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		table.update(object);
	}

	@Override
	public <T> void delete(T object) {
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		table.delete(object);
	}

	@Override
	public <T> int delete(Class<T> clazz, Criteria criteria) {
		Table<T> table = getTable(clazz);
		return table.delete(clazz, criteria);
	}
	
	public <T> void deleteAll(Class<T> clazz) {
		Table<T> table = getTable(clazz);
		table.clear();
	}

	//
	
	private PreparedStatement createStatement(Connection connection, String query, Object[] parameters) throws SQLException {
		PreparedStatement preparedStatement = AbstractTable.createStatement(getConnection(), query, false);
		int param = 1; // !
		for (Object parameter : parameters) {
			setParameter(preparedStatement, param++, parameter);
		}
		return preparedStatement;
	}
	
	public LinkedHashMap<String, PropertyInterface> findColumns(Class<?> clazz) {
		if (columnsForClass.containsKey(clazz)) {
			return columnsForClass.get(clazz);
		}
		
		LinkedHashMap<String, PropertyInterface> columns = new LinkedHashMap<String, PropertyInterface>();
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || FieldUtils.isTransient(field)) continue;
			String fieldName = StringUtils.toSnakeCase(field.getName()).toUpperCase();
			if (StringUtils.equals(fieldName, "ID", "VERSION", "HISTORIZED")) continue;
			if (FieldUtils.isList(field)) continue;
			if (FieldUtils.isFinal(field) && !FieldUtils.isSet(field) && !Codes.isCode(field.getType())) {
				Map<String, PropertyInterface> inlinePropertys = findColumns(field.getType());
				boolean hasClassName = FieldUtils.hasClassName(field) && !FlatProperties.hasCollidingFields(clazz, field.getType(), field.getName());
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = fieldName + "_" + inlineKey;
					}
					key = SqlIdentifier.buildIdentifier(key, getMaxIdentifierLength(), columns.keySet());
					columns.put(key, new ChainedProperty(new FieldProperty(field), inlinePropertys.get(inlineKey)));
				}
			} else {
				fieldName = SqlIdentifier.buildIdentifier(fieldName, getMaxIdentifierLength(), columns.keySet());
				columns.put(fieldName, new FieldProperty(field));
			}
		}
		for (Method method: clazz.getMethods()) {
			if (!Keys.isPublic(method) || Keys.isStatic(method)) continue;
			if (method.getAnnotation(Searched.class) == null && method.getAnnotation(Materialized.class) == null) continue;
			String methodName = method.getName();
			if (!methodName.startsWith("get") || methodName.length() < 4) continue;
			String fieldName = StringUtils.lowerFirstChar(methodName.substring(3));
			String columnName = StringUtils.toSnakeCase(fieldName).toUpperCase();
			columnName = SqlIdentifier.buildIdentifier(columnName, getMaxIdentifierLength(), columns.keySet());
			columns.put(columnName, new Keys.MethodProperty(method.getReturnType(), fieldName, method, null));
		}
		columnsForClass.put(clazz, columns);
		return columns;
	}	
	
	/*
	 * TODO: should be merged with the setParameter in AbstractTable.
	 */
	private void setParameter(PreparedStatement preparedStatement, int param, Object value) throws SQLException {
		if (value instanceof Enum<?>) {
			Enum<?> e = (Enum<?>) value;
			value = e.ordinal();
		} else if (value instanceof LocalDate) {
			value = java.sql.Date.valueOf((LocalDate) value);
		} else if (value instanceof LocalTime) {
			value = java.sql.Time.valueOf((LocalTime) value);
		} else if (value instanceof LocalDateTime) {
			value = java.sql.Timestamp.valueOf((LocalDateTime) value);
		}
		preparedStatement.setObject(param, value);
	}
	
	public <T> List<T> execute(Class<T> clazz, String query, int maxResults, Serializable... parameters) {
		try (PreparedStatement preparedStatement = createStatement(getConnection(), query, parameters)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				List<T> result = new ArrayList<>();
				while (resultSet.next() && result.size() < maxResults) {
					result.add(readResultSetRow(clazz, resultSet));
				}
				return result;
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Couldn't execute query");
		}
	}
	
	public <T> T execute(Class<T> clazz, String query, Serializable... parameters) {
		try (PreparedStatement preparedStatement = createStatement(getConnection(), query, parameters)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				T result = null;
				if (resultSet.next()) {
					result = readResultSetRow(clazz, resultSet);
				}
				return result;
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Couldn't execute query");
		}
	}
	
	public <R> R readResultSetRow(Class<R> clazz, ResultSet resultSet) throws SQLException {
		Map<Class<?>, Map<Object, Object>> loadedReferences = new HashMap<>();
		return readResultSetRow(clazz, resultSet, loadedReferences);
	}
	
	@SuppressWarnings("unchecked")
	public <R> R readResultSetRow(Class<R> clazz, ResultSet resultSet, Map<Class<?>, Map<Object, Object>> loadedReferences) throws SQLException {
		if (clazz == Integer.class) {
			return (R) Integer.valueOf(resultSet.getInt(1));
		} else if (clazz == Long.class) {
			return (R) Long.valueOf(resultSet.getLong(1));
		} else if (clazz == BigDecimal.class) {
			return (R) resultSet.getBigDecimal(1);
		} else if (clazz == String.class) {
			return (R) resultSet.getString(1);
		}
		
		Object id = null;
		Integer position = 0;
		R result = CloneHelper.newInstance(clazz);
		
		LinkedHashMap<String, PropertyInterface> columns = findColumns(clazz);
		
		// first read the resultSet completely then resolve references
		// derby db mixes closing of resultSets.
		
		Map<PropertyInterface, Object> values = new HashMap<>(resultSet.getMetaData().getColumnCount() * 3);
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			String columnName = resultSet.getMetaData().getColumnName(columnIndex).toUpperCase();
			if ("ID".equals(columnName)) {
				id = resultSet.getObject(columnIndex);
				IdUtils.setId(result, id);
				continue;
			} else if ("VERSION".equals(columnName)) {
				IdUtils.setVersion(result, resultSet.getInt(columnIndex));
				continue;
			} else if ("POSITION".equals(columnName)) {
				position = resultSet.getInt(columnIndex);
				continue;				
			}
			
			PropertyInterface property = columns.get(columnName);
			if (property == null) continue;
			
			Class<?> fieldClass = property.getClazz();
			boolean isByteArray = fieldClass.isArray() && fieldClass.getComponentType() == Byte.TYPE;

			Object value = isByteArray ? resultSet.getBytes(columnIndex) : resultSet.getObject(columnIndex);
			if (value == null) continue;
			values.put(property, value);
		}
		
		if (!loadedReferences.containsKey(clazz)) {
			loadedReferences.put(clazz, new HashMap<>());
		}
		Object key = position == null ? id : id + "-" + position;
		if (loadedReferences.get(clazz).containsKey(key)) {
			return (R) loadedReferences.get(clazz).get(key);
		} else {
			loadedReferences.get(clazz).put(key, result);
		}
		
		for (Map.Entry<PropertyInterface, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			PropertyInterface property = entry.getKey();
			if (value != null && !(property instanceof MethodProperty)) {
				Class<?> fieldClass = property.getClazz();
				if (View.class.isAssignableFrom(fieldClass)) {
					Class<?> viewedClass = ViewUtil.getViewedClass(fieldClass);
					if (Code.class.isAssignableFrom(viewedClass)) {
						Class<? extends Code> codeClass = (Class<? extends Code>) viewedClass;
						value = ViewUtil.view(getCode(codeClass, value), CloneHelper.newInstance(fieldClass));
					} else {
						Table<?> referenceTable = getTable(viewedClass);
						value = referenceTable.readView(fieldClass, value, loadedReferences);
					}
				} else if (Code.class.isAssignableFrom(fieldClass)) {
					Class<? extends Code> codeClass = (Class<? extends Code>) fieldClass;
					value = getCode(codeClass, value);
				} else if (IdUtils.hasId(fieldClass)) {
					if (loadedReferences.containsKey(fieldClass) && loadedReferences.get(fieldClass).containsKey(value)) {
						value = loadedReferences.get(fieldClass).get(value);
					} else {
						Table<?> referenceTable = getTable(fieldClass);
						value = referenceTable.read(value, loadedReferences);
					}
				} else if (AbstractTable.isDependable(property)) {
					value = getTable(fieldClass).read(value);
				} else if (fieldClass == Set.class) {
					Set<?> set = (Set<?>) property.getValue(result);
					Class<?> enumClass = property.getGenericClass();
					EnumUtils.fillSet((int) value, enumClass, set);
					continue; // skip setValue, it's final
				} else {
					value = sqlDialect.convertToFieldClass(fieldClass, value);
				}
				property.setValue(result, value);
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public <R> R readResultSetRowPrimitive(Class<R> clazz, ResultSet resultSet) throws SQLException {
		Object value = resultSet.getObject(1);
		return (R) sqlDialect.convertToFieldClass(clazz, value);
	}
	
	//
	
	<U> void addClass(Class<U> clazz) {
		if (!tables.containsKey(clazz)) {
			if (clazz.getSimpleName().startsWith("Swiss")) {
				System.out.println("Hallo");
			}
			tables.put(clazz, null); // break recursion. at some point it is checked if a clazz is already in the tables map.
			Table<U> table = createTable(clazz);
			tables.put(table.getClazz(), table);
		}
	}
	
	<U> Table<U> createTable(Class<U> clazz) {
		return new Table<U>(this, clazz);
	}
	
	void createTables() {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			table.createTable(sqlDialect);
		}
		for (AbstractTable<?> table : tableList) {
			table.createIndexes(sqlDialect);
		}
		for (AbstractTable<?> table : tableList) {
			table.createConstraints(sqlDialect);
		}
	}

	void createCodes() {
		createConstantCodes();
		createCsvCodes();
	}
	
	@SuppressWarnings("unchecked")
	private void createConstantCodes() {
		for (AbstractTable<?> table : tables.values()) {
			if (Code.class.isAssignableFrom(table.getClazz())) {
				Class<? extends Code> codeClass = (Class<? extends Code>) table.getClazz(); 
				List<? extends Code> constants = Codes.getConstants(codeClass);
				for (Code code : constants) {
					((Table<Code>) table).insert(code);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void createCsvCodes() {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			if (Code.class.isAssignableFrom(table.getClazz())) {
				Class<? extends Code> clazz = (Class<? extends Code>) table.getClazz();
				InputStream is = clazz.getResourceAsStream(clazz.getSimpleName() + ".csv");
				if (is != null) {
					CsvReader reader = new CsvReader(is);
					List<? extends Code> values = reader.readValues(clazz);
					for (Code value : values) {
						((Table<Code>) table).insert(value);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <U> AbstractTable<U> getAbstractTable(Class<U> clazz) {
		if (!tables.containsKey(clazz)) {
			throw new IllegalArgumentException(clazz.getName());
		}
		return (AbstractTable<U>) tables.get(clazz);
	}

	public <U> Table<U> getTable(Class<U> clazz) {
		AbstractTable<U> table = getAbstractTable(clazz);
		if (!(table instanceof Table)) throw new IllegalArgumentException(clazz.getName());
		return (Table<U>) table;
	}

	@SuppressWarnings("unchecked")
	public <U> Table<U> getTable(String className) {
		for (Entry<Class<?>, AbstractTable<?>> entry : tables.entrySet()) {
			if (entry.getKey().getName().equals(className)) {
				return (Table<U>) entry.getValue();
			}
		}
		return null;
	}
	
	public String name(Object classOrKey) {
		if (classOrKey instanceof Class) {
			return table((Class<?>) classOrKey);
		} else {
			return column(classOrKey);
		}
	}

	public String table(Class<?> clazz) {
		AbstractTable<?> table = getAbstractTable(clazz);
		return table.getTableName();
	}
	
	public String column(Object key) {
		PropertyInterface property = Keys.getProperty(key);
		Class<?> declaringClass = property.getDeclaringClass();
		if (tables.containsKey(declaringClass)) {
			AbstractTable<?> table = getAbstractTable(declaringClass);
			return table.column(property);
		} else {
			LinkedHashMap<String, PropertyInterface> columns = findColumns(declaringClass);
			for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
				if (StringUtils.equals(entry.getValue().getPath(), property.getPath())) {
					return entry.getKey();
				}
			}
			return null;
		}
	}
	
	public boolean tableExists(Class<?> clazz) {
		return tables.containsKey(clazz);
	}
	
	public <T extends Code> T getCode(Class<T> clazz, Object codeId) {
		if (isLoading(clazz)) {
			// this special case is needed to break a possible reference cycle
			return getTable(clazz).read(codeId);
		}
		List<T> codes = getCodes(clazz);
		return Codes.findCode(codes, codeId);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Code> boolean isLoading(Class<T> clazz) {
		CodeCacheItem<T> cacheItem = (CodeCacheItem<T>) codeCache.get(clazz);
		return cacheItem != null && cacheItem.isLoading();
	}

	@SuppressWarnings("unchecked")
	<T extends Code> List<T> getCodes(Class<T> clazz) {
		synchronized (clazz) {
			CodeCacheItem<T> cacheItem = (CodeCacheItem<T>) codeCache.get(clazz);
			if (cacheItem == null || !cacheItem.isValid()) {
				updateCode(clazz);
			}
			cacheItem = (CodeCacheItem<T>) codeCache.get(clazz);
			List<T> codes = cacheItem.getCodes();
			return codes;
		}
	}

	private <T extends Code> void updateCode(Class<T> clazz) {
		CodeCacheItem<T> codeCacheItem = new CodeCacheItem<T>();
		codeCache.put(clazz, codeCacheItem);
		List<T> codes = find(clazz, By.all());
		codeCacheItem.setCodes(codes);
	}

	public void invalidateCodeCache(Class<?> clazz) {
		codeCache.remove(clazz);
	}

	public int getMaxIdentifierLength() {
		return sqlDialect.getMaxIdentifierLength();
	}

	public Map<String, AbstractTable<?>> getTableByName() {
		return tableByName;
	}
}