package org.minimalj.repository.sql;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.mariadb.jdbc.MySQLDataSource;
import org.minimalj.model.Code;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.FieldProperty;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.test.ModelTest;
import org.minimalj.repository.Repository;
import org.minimalj.repository.criteria.By;
import org.minimalj.repository.criteria.Criteria;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.Codes;
import org.minimalj.util.Codes.CodeCacheItem;
import org.minimalj.util.CsvReader;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * The Mapper to a relationale Database
 * 
 */
public class SqlRepository extends Repository {
	private static final Logger logger = Logger.getLogger(SqlRepository.class.getName());
	public static final boolean CREATE_TABLES = true;
	
	private final SqlSyntax syntax;
	
	private final List<Class<?>> mainClasses;
	private final Map<Class<?>, AbstractTable<?>> tables = new LinkedHashMap<Class<?>, AbstractTable<?>>();
	private final Map<String, AbstractTable<?>> tableByName = new HashMap<String, AbstractTable<?>>();
	private final Map<Class<?>, LinkedHashMap<String, PropertyInterface>> columnsForClass = new HashMap<>(200);
	
	private final DataSource dataSource;
	
	private Connection autoCommitConnection;
	private final BlockingDeque<Connection> connectionDeque = new LinkedBlockingDeque<>();
	private final ThreadLocal<Connection> threadLocalTransactionConnection = new ThreadLocal<>();

	private final HashMap<Class<? extends Code>, CodeCacheItem<? extends Code>> codeCache = new HashMap<>();
	
	public SqlRepository(DataSource dataSource, Class<?>... classes) {
		this(dataSource, createTablesOnInitialize(dataSource), classes);
	}

	public SqlRepository(DataSource dataSource, boolean createTablesOnInitialize, Class<?>... classes) {
		this.dataSource = dataSource;
		this.mainClasses = Arrays.asList(classes);
		Connection connection = getAutoCommitConnection();
		try {
			String databaseProductName = connection.getMetaData().getDatabaseProductName();
			boolean isMySqlDb = StringUtils.equals(databaseProductName, "MySQL");
			boolean isDerbyDb = StringUtils.equals(databaseProductName, "Apache Derby");
			if (isMySqlDb) {
				syntax = new SqlSyntax.MariaSqlSyntax();
			} else if (isDerbyDb) {
				syntax = new SqlSyntax.DerbySqlSyntax();
			} else {
				throw new RuntimeException("Only MySQL/MariaDB and Derby DB supported at the moment");
			}
			for (Class<?> clazz : classes) {
				addClass(clazz);
			}
			testModel(classes);
			if (createTablesOnInitialize) {
				createTables();
				createCodes();
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Could not determine product name of database");
		}
	}
	
	/**
	 * Convinience method for prototyping and testing
	 * @return a DataSource representing a in memory database managed by derby db.
	 */
	public static DataSource embeddedDataSource() {
		return embeddedDataSource(null);
	}
	
	// every JUnit test must have a fresh memory db
	private static int memoryDbCount = 1;
	
	public static DataSource embeddedDataSource(String file) {
		EmbeddedDataSource dataSource;
		try {
			dataSource = new EmbeddedDataSource();
		} catch (NoClassDefFoundError e) {
			logger.severe("Missing EmbeddedDataSource. Please ensure to have derby in the classpath");
			throw new IllegalStateException("Configuration error: Missing EmbeddedDataSource");
		}
		
		dataSource.setDatabaseName(file != null ? file : "memory:TempDB" + (memoryDbCount++));
		dataSource.setCreateDatabase("create");
		return dataSource;
	}
	
	public static DataSource mariaDbDataSource(String database, String user, String password) {
		try {
			MySQLDataSource dataSource = new MySQLDataSource("localhost", 3306, database);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			return dataSource;
		} catch (NoClassDefFoundError e) {
			logger.severe("Missing MySQLDataSource. Please ensure to have mariadb-java-client in the classpath");
			throw new IllegalStateException("Configuration error: Missing MySQLDataSource");
		}
		
	}
	
//	private void connectToCloudFoundry() throws ClassNotFoundException, SQLException, JSONException {
//		String vcap_services = System.getenv("VCAP_SERVICES");
//		
//		if (vcap_services != null && vcap_services.length() > 0) {
//			JSONObject root = new JSONObject(vcap_services);
//
//			JSONArray mysqlNode = (JSONArray) root.get("mysql-5.1");
//			JSONObject firstSqlNode = (JSONObject) mysqlNode.get(0);
//			JSONObject credentials = (JSONObject) firstSqlNode.get("credentials");
//
//			String dbname = credentials.getString("name");
//			String hostname = credentials.getString("hostname");
//			String user = credentials.getString("user");
//			String password = credentials.getString("password");
//			String port = credentials.getString("port");
//			
//			String dbUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbname;
//
//			Class.forName("com.mysql.jdbc.Driver");
//			connection = DriverManager.getConnection(dbUrl, user, password);
//			
//			isDerbyDb = false;
//			isDerbyMemoryDb = false;
//			isMySqlDb = true;
//		}
//	}

	private Connection getAutoCommitConnection() {
		try {
			// problem with isValid in maria db driver < 1.1.8 
			// if (autoCommitConnection == null || !autoCommitConnection.isValid(0)) {
			if (autoCommitConnection == null) {
				autoCommitConnection = dataSource.getConnection();
				autoCommitConnection.setAutoCommit(true);
			}
			return autoCommitConnection;
		} catch (Exception e) {
			throw new LoggingRuntimeException(e, logger, "Not possible to create autocommit connection");
		}
	}

	public boolean isMainClasses(Class<?> clazz) {
		return mainClasses.contains(clazz);
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
	
	private static boolean createTablesOnInitialize(DataSource dataSource) {
		return dataSource instanceof EmbeddedDataSource && "create".equals(((EmbeddedDataSource) dataSource).getCreateDatabase());
	}
	
	@Override
	public <T> T read(Class<T> clazz, Object id) {
		Table<T> table = getTable(clazz);
		return table.read(id);
	}

	public <T> T readVersion(Class<T> clazz, Object id, Integer time) {
		HistorizedTable<T> table = (HistorizedTable<T>) getTable(clazz);
		return table.read(id, time);
	}

	@Override
	public <T> List<T> read(Class<T> resultClass, Criteria criteria, int maxResults) {
		if (View.class.isAssignableFrom(resultClass)) {
			Class<?> viewedClass = ViewUtil.getViewedClass(resultClass);
			Table<?> table = getTable(viewedClass);
			return table.readView(resultClass, criteria, maxResults);
		} else {
			Table<T> table = getTable(resultClass);
			return table.read(criteria, maxResults);
		}
	}

	@Override
	public <T> Object insert(T object) {
		if (object == null) throw new NullPointerException();
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		return table.insert(object);
	}

	@Override
	public <T> void update(T object) {
		if (object == null) throw new NullPointerException();
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		table.update(object);
	}

	public <T> void delete(T object) {
		delete(object.getClass(), IdUtils.getId(object));
	}

	@Override
	public <T> void delete(Class<T> clazz, Object id) {
		Table<T> table = getTable(clazz);
		// TODO do in transaction and merge with insert/update
		table.delete(id);
	}
	
	public <T> void deleteAll(Class<T> clazz) {
		Table<T> table = getTable(clazz);
		table.clear();
	}

	public <T> List<T> loadHistory(Class<?> clazz, Object id, int maxResult) {
		@SuppressWarnings("unchecked")
		Table<T> table = (Table<T>) getTable(clazz);
		if (table instanceof HistorizedTable) {
			HistorizedTable<T> historizedTable = (HistorizedTable<T>) table;
			int maxVersion = historizedTable.getMaxVersion(id);
			int maxResults = Math.min(maxVersion + 1, maxResult);
			List<T> result = new ArrayList<>(maxResults);
			for (int i = 0; i<maxResults; i++) {
				result.add(historizedTable.read(id, maxVersion - i));
			}
			return result;
		} else {
			throw new IllegalArgumentException(clazz.getSimpleName() + " is not historized");
		}
	}

	@Override
	public <ELEMENT, PARENT> List<ELEMENT> getList(LazyList<PARENT, ELEMENT> list) {
		CrossTable<?, ELEMENT> subTable = (CrossTable<?, ELEMENT>) getTableByName().get(list.getListName());
		return subTable.readAll(list.getParentId());
	}
	
	@Override
	public <ELEMENT, PARENT> ELEMENT add(LazyList<PARENT, ELEMENT> list, ELEMENT element) {
		CrossTable<?, ELEMENT> subTable = (CrossTable<?, ELEMENT>) getTableByName().get(list.getListName());
		return subTable.addElement(list.getParentId(), element);
	}
	
	@Override
	public <ELEMENT, PARENT> void remove(LazyList<PARENT, ELEMENT> list, int position) {
		throw new RuntimeException("Not yet implemented");
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
					key = SqlHelper.buildName(key, getMaxIdentifierLength(), columns.keySet());
					columns.put(key, new ChainedProperty(new FieldProperty(field), inlinePropertys.get(inlineKey)));
				}
			} else {
				fieldName = SqlHelper.buildName(fieldName, getMaxIdentifierLength(), columns.keySet());
				columns.put(fieldName, new FieldProperty(field));
			}
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
					result = readResultSetRow(clazz, resultSet, null);
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
		} else if (clazz == BigDecimal.class) {
			return (R) resultSet.getBigDecimal(1);
		} else if (clazz == String.class) {
			return (R) resultSet.getString(1);
		}
		
		R result = CloneHelper.newInstance(clazz);
		
		SqlHelper helper = new SqlHelper(this);
		LinkedHashMap<String, PropertyInterface> columns = findColumns(clazz);
		
		// first read the resultSet completly then resolve references
		// derby db mixes closing of resultSets.
		
		Map<PropertyInterface, Object> values = new HashMap<>(resultSet.getMetaData().getColumnCount() * 3);
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			String columnName = resultSet.getMetaData().getColumnName(columnIndex);
			if ("ID".equalsIgnoreCase(columnName)) {
				IdUtils.setId(result, resultSet.getObject(columnIndex));
				continue;
			} else if ("VERSION".equalsIgnoreCase(columnName)) {
				IdUtils.setVersion(result, resultSet.getInt(columnIndex));
				continue;
			} else if ("HISTORIZED".equalsIgnoreCase(columnName)) {
				IdUtils.setHistorized(result, resultSet.getInt(columnIndex));
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
		
		for (Map.Entry<PropertyInterface, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			PropertyInterface property = entry.getKey();
			if (value != null) {
				Class<?> fieldClass = property.getClazz();
				if (Code.class.isAssignableFrom(fieldClass)) {
					Class<? extends Code> codeClass = (Class<? extends Code>) fieldClass;
					value = getCode(codeClass, value);
				} else if (View.class.isAssignableFrom(fieldClass)) {
					Class<?> viewedClass = ViewUtil.getViewedClass(fieldClass);
					Table<?> referenceTable = getTable(viewedClass);
					value = referenceTable.readView(fieldClass, value);
				} else if (IdUtils.hasId(fieldClass)) {
					if (!loadedReferences.containsKey(fieldClass)) {
						loadedReferences.put(fieldClass, new HashMap<>());
					}
					if (loadedReferences.get(fieldClass).containsKey(value)) {
						value = loadedReferences.get(fieldClass).get(value);
					} else {
						Table<?> referenceTable = getTable(fieldClass);
						Object reference = referenceTable.read(value);
						loadedReferences.get(fieldClass).put(value, reference);
						value = reference;
					}
				} else if (SqlHelper.isDependable(property)) {
					value = getTable(fieldClass).read(value);
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
	
	//
	
	<U> void addClass(Class<U> clazz) {
		if (!tables.containsKey(clazz)) {
			boolean historized = FieldUtils.hasValidHistorizedField(clazz);
			tables.put(clazz, null); // break recursion. at some point it is checked if a clazz is already in the tables map.
			Table<U> table = historized ? new HistorizedTable<U>(this, clazz) : new Table<U>(this, clazz);
			tables.put(table.getClazz(), table);
		}
	}
	
	private void createTables() {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			table.createTable(syntax);
		}
		for (AbstractTable<?> table : tableList) {
			table.createIndexes(syntax);
		}
		for (AbstractTable<?> table : tableList) {
			table.createConstraints(syntax);
		}
	}

	private void createCodes() {
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

	public <U> Table<U> getTable(String className) {
		for (Entry<Class<?>, AbstractTable<?>> entry : tables.entrySet()) {
			if (entry.getKey().getName().equals(className)) {
				return (Table) entry.getValue();
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
		AbstractTable<?> table = getAbstractTable(declaringClass);
		return table.column(property);
	}
	
	public boolean tableExists(Class<?> clazz) {
		return tables.containsKey(clazz);
	}
	
	private void testModel(Class<?>[] classes) {
		ModelTest test = new ModelTest(classes);
		if (!test.getProblems().isEmpty()) {
			test.logProblems();
			throw new IllegalArgumentException("The persistent classes don't apply to the given rules");
		}
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
		List<T> codes = getTable(clazz).read(By.all(), Integer.MAX_VALUE);
		codeCacheItem.setCodes(codes);
	}

	public void invalidateCodeCache(Class<?> clazz) {
		codeCache.remove(clazz);
	}

	public int getMaxIdentifierLength() {
		return syntax.getMaxIdentifierLength();
	}
	
	public Map<String, AbstractTable<?>> getTableByName() {
		return tableByName;
	}

}