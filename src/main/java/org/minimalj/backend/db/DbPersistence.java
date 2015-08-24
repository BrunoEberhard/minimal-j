package org.minimalj.backend.db;

import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.mariadb.jdbc.MySQLDataSource;
import org.minimalj.application.DevMode;
import org.minimalj.backend.Persistence;
import org.minimalj.model.Code;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.test.ModelTest;
import org.minimalj.transaction.criteria.Criteria;
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
public class DbPersistence implements Persistence {
	private static final Logger logger = Logger.getLogger(DbPersistence.class.getName());
	public static final boolean CREATE_TABLES = true;
	
	private final DbSyntax syntax;
	
	private final Map<Class<?>, AbstractTable<?>> tables = new LinkedHashMap<Class<?>, AbstractTable<?>>();
	private final Set<String> tableNames = new HashSet<>();
	
	private final DataSource dataSource;
	
	private Connection autoCommitConnection;
	private final BlockingDeque<Connection> connectionDeque = new LinkedBlockingDeque<>();
	private final ThreadLocal<Connection> threadLocalTransactionConnection = new ThreadLocal<>();

	private final HashMap<Class<? extends Code>, CodeCacheItem<? extends Code>> codeCache = new HashMap<>();
	
	private final Map<String, String> queries = new HashMap<>();

	public DbPersistence(DataSource dataSource, Class<?>... classes) {
		this(dataSource, createTablesOnInitialize(dataSource), classes);
	}

	public DbPersistence(DataSource dataSource, boolean createTablesOnInitialize, Class<?>... classes) {
		this.dataSource = dataSource;
		Connection connection = getAutoCommitConnection();
		try {
			String databaseProductName = connection.getMetaData().getDatabaseProductName();
			boolean isMySqlDb = StringUtils.equals(databaseProductName, "MySQL");
			boolean isDerbyDb = StringUtils.equals(databaseProductName, "Apache Derby");
			if (isMySqlDb) {
				syntax = new DbSyntax.MariaDbSyntax();
			} else if (isDerbyDb) {
				syntax = new DbSyntax.DerbyDbSyntax();
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
		// this.queries = Application.getApplication().getQueries();
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

	public synchronized void startTransaction() {
		if (threadLocalTransactionConnection.get() != null) return;
		
		Connection transactionConnection = allocateConnection();
		threadLocalTransactionConnection.set(transactionConnection);
	}

	public synchronized void endTransaction(boolean commit) {
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
	
	private Connection allocateConnection() {
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
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
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
			return getAutoCommitConnection();
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
	
	public <T> T read(Class<T> clazz, Object id, Integer time) {
		HistorizedTable<T> table = (HistorizedTable<T>) table(clazz);
		return table.read(id, time);
	}

	public List<Integer> readVersions(Class<?> clazz, Object id) {
		HistorizedTable<?> table = (HistorizedTable<?>) table(clazz);
		return table.readVersions(id);
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

	public <T> Object insert(T object) {
		if (object == null) throw new NullPointerException();
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		return (T) table.insert(object);
	}

	@Override
	public <T> T update(T object) {
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		if (isTransactionActive()) {
			table.update(object);
		} else {
			boolean runThrough = false;
			try {
				startTransaction();
				table.update(object);
				runThrough = true;
			} finally {
				endTransaction(runThrough);
			}
		}
		// re read result
		return table.read(IdUtils.getId(object));
	}

	public <T> void delete(T object) {
		delete(object.getClass(), IdUtils.getId(object));
	}

	@Override
	public <T> void delete(Class<T> clazz, Object id) {
		Table<T> table = getTable(clazz);
		table.delete(id);
	}
	
	public <T> void deleteAll(Class<T> clazz) {
		Table<T> table = getTable(clazz);
		table.clear();
	}

//	public <T> T read(Class<T> clazz, Object id, Integer time) {
//		AbstractTable<T> abstractTable = persistence.table(clazz);
//		if (abstractTable instanceof HistorizedTable) {
//			return ((HistorizedTable<T>) abstractTable).read(id, time);
//		} else {
//			throw new IllegalArgumentException(clazz + " is not historized");
//		}
//	}

	public <T> List<T> loadHistory(Class<?> clazz, Object id, int maxResult) {
		// TODO maxResults is ignored in loadHistory
		@SuppressWarnings("unchecked")
		AbstractTable<T> abstractTable = (AbstractTable<T>) table(clazz);
		if (abstractTable instanceof HistorizedTable) {
			List<Integer> times = ((HistorizedTable<T>) abstractTable).readVersions(id);
			List<T> result = new ArrayList<>();
			for (int time : times) {	
				result.add(((HistorizedTable<T>) abstractTable).read(id, time));
			}
			return result;
		} else {
			throw new IllegalArgumentException(clazz.getSimpleName() + " is not historized");
		}
	}
	
	@Override
	public <T> T executeStatement(Class<T> clazz, String queryName, Serializable... parameters) {
		String query = getQuery(queryName);
		try (PreparedStatement preparedStatement = createStatement(getConnection(), query, parameters)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				T result = null;
				if (resultSet.next()) {
					result = readResultRow(resultSet, clazz);
				}
				return result;
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Couldn't execute query");
		}
	}
	
	@Override
	public <T> List<T> executeStatement(Class<T> clazz, String queryName, int maxResults, Serializable... parameters) {
		String query = getQuery(queryName);
		try (PreparedStatement preparedStatement = createStatement(getConnection(), query, parameters)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				List<T> result = new ArrayList<>();
				while (resultSet.next() && result.size() < maxResults) {
					result.add(readResultRow(resultSet, clazz));
				}
				return result;
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Couldn't execute query");
		}
	}
	
	private String getQuery(String queryName) {
		if (queries == null) {
			throw new RuntimeException("No queries available: " + queryName);
		} else if (!queries.containsKey(queryName)) {
			throw new RuntimeException("Query not available: " + queryName);
		}
		return queries.get(queryName);
	}
	
	private PreparedStatement createStatement(Connection connection, String query, Object[] parameters) throws SQLException {
		PreparedStatement preparedStatement = AbstractTable.createStatement(getConnection(), query, false);
		int param = 1; // !
		for (Object parameter : parameters) {
			setParameter(preparedStatement, param++, parameter);
		}
		return preparedStatement;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T readResultRow(ResultSet resultSet, Class<T> clazz) throws SQLException {
		if (clazz == Integer.class) {
			return (T) Integer.valueOf(resultSet.getInt(1));
		} else if (clazz == BigDecimal.class) {
			return (T) resultSet.getBigDecimal(1);
		} else if (clazz == String.class) {
			return (T) resultSet.getString(1);
		} else {
			throw new IllegalArgumentException(clazz.getName());
		}
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
	
	//
	
	@SuppressWarnings("unchecked")
	<U> AbstractTable<U> addClass(Class<U> clazz) {
		if (!tables.containsKey(clazz)) {
			boolean historized = FieldUtils.hasValidVersionfield(clazz);
			tables.put(clazz, null); // break recursion. at some point it is check if a clazz is alread in the tables map.
			Table<U> table = historized ? new HistorizedTable<U>(this, clazz) : new Table<U>(this, clazz);
			tables.put(table.getClazz(), table);
			tableNames.add(table.getTableName());
		}
		return (AbstractTable<U>) tables.get(clazz);
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
	public <U> AbstractTable<U> table(Class<U> clazz) {
		if (!tables.containsKey(clazz)) {
			throw new IllegalArgumentException(clazz.getName());
		}
		return (AbstractTable<U>) tables.get(clazz);
	}

	public <U> Table<U> getTable(Class<U> clazz) {
		AbstractTable<U> table = table(clazz);
		if (!(table instanceof Table)) throw new IllegalArgumentException(clazz.getName());
		return (Table<U>) table;
	}

	public boolean tableExists(Class<?> clazz) {
		return tables.containsKey(clazz);
	}
	
	private void testModel(Class<?>[] classes) {
		ModelTest test = new ModelTest(classes);
		if (DevMode.isActive()) {
			test.printMissingResources();
		}
		if (!test.getProblems().isEmpty()) {
			for (String s : test.getProblems()) {
				logger.severe(s);
			}
			throw new IllegalArgumentException("The persistent classes don't apply to the given rules");
		}
	}

	public <T extends Code> T getCode(Class<T> clazz, Object codeId) {
		return getCode(clazz, codeId, true);
	}

	<T extends Code> T getCode(Class<T> clazz, Object codeId, boolean forceCache) {
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
		List<T> codes = getTable(clazz).read(Criteria.all(), Integer.MAX_VALUE);
		codeCacheItem.setCodes(codes);
	}

	public void invalidateCodeCache(Class<?> clazz) {
		codeCache.remove(clazz);
	}

	public int getMaxIdentifierLength() {
		return syntax.getMaxIdentifierLength();
	}
	
	public Set<String> getTableNames() {
		return tableNames;
	}

}
