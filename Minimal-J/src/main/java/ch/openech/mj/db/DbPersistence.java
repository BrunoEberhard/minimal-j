package ch.openech.mj.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

import ch.openech.mj.model.test.ModelTest;
import ch.openech.mj.util.LoggingRuntimeException;
import ch.openech.mj.util.StringUtils;

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
	
	private Boolean initialized = false;
	
	private final boolean isDerbyDb;
	private final boolean isMySqlDb; 
	private final boolean createTablesOnInitialize;
	
	private final Map<Class<?>, AbstractTable<?>> tables = new LinkedHashMap<Class<?>, AbstractTable<?>>();
	private final Set<Class<?>> immutables = new HashSet<>();
	
	private final DataSource dataSource;
	
	private Connection autoCommitConnection;
	private BlockingDeque<Connection> connectionDeque = new LinkedBlockingDeque<>();
	private ThreadLocal<Connection> threadLocalTransactionConnection = new ThreadLocal<>();

	public DbPersistence(DataSource dataSource) {
		this(dataSource, createTablesOnInitialize(dataSource));
	}

	public DbPersistence(DataSource dataSource, boolean createTablesOnInitialize) {
		this.dataSource = dataSource;
		Connection connection = getAutoCommitConnection();
		try {
			String databaseProductName = connection.getMetaData().getDatabaseProductName();
			isMySqlDb = StringUtils.equals(databaseProductName, "MySQL");
			isDerbyDb = StringUtils.equals(databaseProductName, "Apache Derby");
			if (!isMySqlDb && !isDerbyDb) throw new RuntimeException("Only MySQL/MariaDB and Derby DB supported at the moment");
			this.createTablesOnInitialize = createTablesOnInitialize;
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

	public <V> V transaction(Transaction<V> transaction, String description) {
		Connection transactionConnection = beginTransaction();
		threadLocalTransactionConnection.set(transactionConnection);
		boolean runThrough = false;
		V result;
		try {
			result = transaction.execute();
			runThrough = true;
		} finally {
			try {
				if (runThrough) {
					transactionConnection.commit();
				} else {
					transactionConnection.rollback();
				}
				endTransaction(transactionConnection);
			} catch (SQLException e) {
				// TODO if commit/rollback throws exception, what should be done?
				try {
					transactionConnection.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} finally {
				threadLocalTransactionConnection.set(null);
			}
		}
		return result;
	}
	
	private Connection beginTransaction() {
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
	
	private void endTransaction(Connection connection) {
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
		if (!initialized) {
			initialize();
		}
		Connection connection = threadLocalTransactionConnection.get();
		if (connection != null) {
			return connection;
		} else {
			return getAutoCommitConnection();
		}
	}
	
	private void initialize() {
		synchronized (initialized) {
			if (!initialized) {
				initialized = true;
				testModel();
				if (createTablesOnInitialize) {
					createTables();
				}
			}
		}
	}
	
	private static boolean createTablesOnInitialize(DataSource dataSource) {
		return dataSource instanceof EmbeddedDataSource && "create".equals(((EmbeddedDataSource) dataSource).getCreateDatabase());
	}
	
	public <T> T read(Class<T> clazz, int id) {
		Table<T> table = (Table<T>) getTable(clazz);
		return table.read(id);
	}
	
	public <T> T read(Class<T> clazz, int id, Integer time) {
		HistorizedTable<T> table = (HistorizedTable<T>) getTable(clazz);
		return table.read(id, time);
	}

	public List<Integer> readVersions(Class<?> clazz, int id) {
		HistorizedTable<?> table = (HistorizedTable<?>) getTable(clazz);
		return table.readVersions(id);
	}

	public <T> int insert(T object) {
		if (object != null) {
			@SuppressWarnings("unchecked")
			Table<T> table = (Table<T>) getTable(object.getClass());
			return table.insert(object);
		} else {
			return 0;
		}
	}
	
	public <T> void update(T object) {
		@SuppressWarnings("unchecked")
		Table<T> table = (Table<T>) getTable(object.getClass());
		table.update(object);
	}
	
	public <T> void delete(T object) {
		@SuppressWarnings("unchecked")
		Table<T> table = (Table<T>) getTable(object.getClass());
		table.delete(object);
	}
	
	public <T> List<Integer> findIds(Class<T> clazz, Object field, Object query) {
		return findIds(clazz, field, query);
	}

	public boolean isDerbyDb() {
		return isDerbyDb;
	}

	public boolean isMySqlDb() {
		return isMySqlDb;
	}
	
	//

	private void add(AbstractTable<?> table) {
		if (initialized) {
			throw new IllegalStateException("Not allowed to add Table after connecting");
		}
		tables.put(table.getClazz(), table);
	}
	
	public <U> Table<U> addClass(Class<U> clazz) {
		Table<U> table = new Table<U>(this, clazz);
		add(table);
		return table;
	}
	
	public <U> HistorizedTable<U> addHistorizedClass(Class<U> clazz) {
		HistorizedTable<U> table = new HistorizedTable<U>(this, clazz);
		add(table);
		return table;
	}
	
	public void addIdentificationClass(Class<?> clazz) {
		addImmutableClass(clazz);
	}
	
	/**
	 * @param clazz objects of this class will not be inlined
	 * @return
	 */
	public <U> ImmutableTable<U> addImmutableClass(Class<U> clazz) {
		immutables.add(clazz);
		ImmutableTable<U> table = new ImmutableTable<U>(this, clazz);
		add(table);
		return table;
	}
	
	public boolean isImmutable(Class<?> clazz) {
		return immutables.contains(clazz);
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
	public <U> AbstractTable<U> getTable(Class<U> clazz) {
		return (AbstractTable<U>) tables.get(clazz);
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
