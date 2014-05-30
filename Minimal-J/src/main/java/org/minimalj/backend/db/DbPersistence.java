package org.minimalj.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.mariadb.jdbc.MySQLDataSource;
import org.minimalj.model.test.ModelTest;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

/**
 * Most important class of the persistence layer.
 * <OL>
 * <LI>Add your classes with addClass and andHistorizedClass
 * <LI>Call connect
 * </OL>
 * 
 * @author bruno
 *
 */
public class DbPersistence {
	private static final Logger logger = Logger.getLogger(DbPersistence.class.getName());
	public static final boolean CREATE_TABLES = true;
	
	private final boolean isDerbyDb;
	private final boolean isMySqlDb; 
	private final boolean createTablesOnInitialize;
	
	private final Map<Class<?>, AbstractTable<?>> tables = new LinkedHashMap<Class<?>, AbstractTable<?>>();
	
	private final DataSource dataSource;
	
	private Connection autoCommitConnection;
	private BlockingDeque<Connection> connectionDeque = new LinkedBlockingDeque<>();
	private ThreadLocal<Connection> threadLocalTransactionConnection = new ThreadLocal<>();

	public DbPersistence(DataSource dataSource, Class<?>... classes) {
		this(dataSource, createTablesOnInitialize(dataSource), classes);
	}

	public DbPersistence(DataSource dataSource, boolean createTablesOnInitialize, Class<?>... classes) {
		this.dataSource = dataSource;
		Connection connection = getAutoCommitConnection();
		try {
			String databaseProductName = connection.getMetaData().getDatabaseProductName();
			isMySqlDb = StringUtils.equals(databaseProductName, "MySQL");
			isDerbyDb = StringUtils.equals(databaseProductName, "Apache Derby");
			if (!isMySqlDb && !isDerbyDb) throw new RuntimeException("Only MySQL/MariaDB and Derby DB supported at the moment");
			this.createTablesOnInitialize = createTablesOnInitialize;
			for (Class<?> clazz : classes) {
				addClass(clazz);
			}
			initialize();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Could not determine product name of database");
		}
	}
	
	private static int memoryDbCount = 1;
	
	public static DataSource embeddedDataSource() {
		EmbeddedDataSource dataSource = new EmbeddedDataSource();
		dataSource.setDatabaseName("memory:TempDB" + (memoryDbCount++)); // for FileSystem use "data/testdb"
		dataSource.setCreateDatabase("create");
		return dataSource;
	}
	
	public static DataSource mariaDbDataSource(String database, String user, String password) {
		MySQLDataSource dataSource = new MySQLDataSource("localhost", 3306, database);
		dataSource.setUser(user);
		dataSource.setPassword(password);
		return dataSource;
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
			if (autoCommitConnection == null || !autoCommitConnection.isValid(0)) {
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
			if (!(table instanceof ImmutableTable)) {
				table.clear();
			}
		}
		for (AbstractTable<?> table : tableList) {
			if (table instanceof ImmutableTable) {
				table.clear();
			}
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
	
	private void initialize() {
		testModel();
		if (createTablesOnInitialize) {
			createTables();
		}
	}
	
	private static boolean createTablesOnInitialize(DataSource dataSource) {
		return dataSource instanceof EmbeddedDataSource && "create".equals(((EmbeddedDataSource) dataSource).getCreateDatabase());
	}
	
	public <T> T read(Class<T> clazz, long id) {
		Table<T> table = getTable(clazz);
		return table.read(id);
	}
	
	public <T> T read(Class<T> clazz, long id, Integer time) {
		HistorizedTable<T> table = (HistorizedTable<T>) table(clazz);
		return table.read(id, time);
	}

	public List<Integer> readVersions(Class<?> clazz, long id) {
		HistorizedTable<?> table = (HistorizedTable<?>) table(clazz);
		return table.readVersions(id);
	}

	public <T> long insert(T object) {
		if (object != null) {
			@SuppressWarnings("unchecked")
			Table<T> table = getTable((Class<T>) object.getClass());
			return table.insert(object);
		} else {
			return 0;
		}
	}
	
	public <T> void update(T object) {
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		table.update(object);
	}
	
	public <T> void delete(T object) {
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		table.delete(object);
	}
	
	public boolean isDerbyDb() {
		return isDerbyDb;
	}

	public boolean isMySqlDb() {
		return isMySqlDb;
	}
	
	public <T> T execute(Class<T> clazz, String query, Object... parameters) {
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
	
	public <T> List<T> execute(Class<T> clazz, String query, int maxResults, Object... parameters) {
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
	
	private PreparedStatement createStatement(Connection connection, String query, Object[] parameters) throws SQLException {
		PreparedStatement preparedStatement = AbstractTable.createStatement(getConnection(), query, false);
		int param = 1; // !
		for (Object parameter : parameters) {
			setParameter(preparedStatement, param++, parameter);
		}
		return preparedStatement;
	}
	
	private <T> T readResultRow(ResultSet resultSet, Class<T> clazz) throws SQLException {
		if (clazz == Integer.class) {
			return (T) Integer.valueOf(resultSet.getInt(1));
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
			value = DbPersistenceHelper.convertToSql((LocalDate) value);
		} else if (value instanceof LocalTime) {
			value = DbPersistenceHelper.convertToSql((LocalTime) value);
		} else if (value instanceof LocalDateTime) {
			value = DbPersistenceHelper.convertToSql((LocalDateTime) value);
		}
		preparedStatement.setObject(param, value);
	}
	
	//

	private void add(AbstractTable<?> table) {
		tables.put(table.getClazz(), table);
	}
	
	private <U> AbstractTable<U> addClass(Class<U> clazz) {
		if (FieldUtils.hasValidIdfield(clazz)) {
			if (FieldUtils.hasValidVersionfield(clazz)) {
				return addHistorizedClass(clazz);
			} else {
				Table<U> table = new Table<U>(this, clazz);
				add(table);
				return table;
			}
		} else {
			throw new IllegalArgumentException(clazz.getName() + " has no valid id field");
		}
	}

	private <U> HistorizedTable<U> addHistorizedClass(Class<U> clazz) {
		HistorizedTable<U> table = new HistorizedTable<U>(this, clazz);
		add(table);
		return table;
	}
	
	/**
	 * @param clazz objects of this class will not be inlined
	 * @return
	 */
	<U> ImmutableTable<U> addImmutableClass(Class<U> clazz) {
		ImmutableTable<U> table = new ImmutableTable<U>(this, clazz);
		add(table);
		return table;
	}
	
	private void createTables() {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			try {
				table.create();
			} catch (SQLException x) {
				throw new LoggingRuntimeException(x, logger, "Couldn't initialize table: " + table.getTableName());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <U> ImmutableTable<U> getImmutableTable(Class<U> clazz) {
		return (ImmutableTable<U>) tables.get(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <U> AbstractTable<U> table(Class<U> clazz) {
		if (!tables.containsKey(clazz)) throw new IllegalArgumentException(clazz.getName());
		return (AbstractTable<U>) tables.get(clazz);
	}

	public <U> Table<U> getTable(Class<U> clazz) {
		AbstractTable<U> table = table(clazz);
		if (!(table instanceof Table)) throw new IllegalArgumentException(clazz.getName());
		return (Table<U>) table;
	}

	private void testModel() {
		List<Class<?>> mainModelClasses = new ArrayList<>();
		for (Map.Entry<Class<?>, AbstractTable<?>> entry : tables.entrySet()) {
			if (!(entry.getValue() instanceof ImmutableTable)) {
				mainModelClasses.add(entry.getKey());
			}
		}
		ModelTest test = new ModelTest(mainModelClasses);
		if (!test.getProblems().isEmpty()) {
			for (String s : test.getProblems()) {
				logger.severe(s);
			}
			throw new IllegalArgumentException("The persistent classes don't apply to the given rules");
		}
	}
	
}
