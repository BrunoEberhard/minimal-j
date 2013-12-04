package ch.openech.mj.db;

import java.sql.Connection;
import java.sql.DriverManager;
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
import org.apache.derby.jdbc.EmbeddedDriver;
import org.mariadb.jdbc.MySQLDataSource;

import ch.openech.mj.model.test.ModelTest;

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
	
	// private static final String DEFAULT_URL = "jdbc:derby:memory:TempDB;create=true";
	public static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/openech?user=APP&password=APP"; 

	private boolean initialized = false;
	
	private boolean isDerbyDb;
	private boolean isDerbyMemoryDb;
	private boolean isMySqlDb; 
	
	private final Map<Class<?>, AbstractTable<?>> tables = new LinkedHashMap<Class<?>, AbstractTable<?>>();
	private final Set<Class<?>> immutables = new HashSet<>();
	
	private final DataSource dataSource;
	
	private Connection autoCommitConnection;
	private BlockingDeque<Connection> daoDeque = new LinkedBlockingDeque<>();
	private ThreadLocal<Connection> transaction = new ThreadLocal<>();
	
	/**
	 * Only creates the persistence. Does not yet connect to the DB.
	 */
	public DbPersistence(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	private static int memoryDbCount = 1;
	
	public static DataSource embeddedDataSource() {
		try {
			DriverManager.registerDriver(new EmbeddedDriver());
//			DriverManager.getConnection("jdbc:derby:memory:testdb;create=true", "", "");
//			DriverManager.getConnection("jdbc:derby:data/testdb;create=true", "", "");

			EmbeddedDataSource dataSource = new EmbeddedDataSource();
			dataSource.setUser("");
			dataSource.setPassword("");
//			ds.setDatabaseName("data/testdb");
			dataSource.setDatabaseName("memory:TempDB" + (memoryDbCount++));
			dataSource.setCreateDatabase("create");
			return dataSource;
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Creation of DataSource failed", e);
			throw new RuntimeException("Creation of DataSource failed");
		}
	}
	
	public static DataSource mariaDbDataSource() {
		try {
			DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
			MySQLDataSource dataSource = new MySQLDataSource("localhost", 3306, "OpenEch");
			dataSource.setUser("APP");
			dataSource.setPassword("APP");
			dataSource.setServerName("localhost");
			dataSource.setDatabaseName("OpenEch");
			return dataSource;
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Creation of DataSource failed", e);
			throw new RuntimeException("Creation of DataSource failed");
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
			if (autoCommitConnection == null || !autoCommitConnection.isValid(0)) {
				autoCommitConnection = dataSource.getConnection();
				autoCommitConnection.setAutoCommit(true);
			}
			return autoCommitConnection;
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Not possible to create autocommit connection", e);
			throw new RuntimeException("Not possible to create autocommit connection");
		}
	}
	
	public void transaction(Runnable runnable) throws Exception {
		Connection transactionConnection = beginTransaction();
		transaction.set(transactionConnection);
		Exception exception = null;
		try {
			runnable.run();
			transactionConnection.commit();
		} catch (Exception x) {
			exception = x;
			try {
				transactionConnection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		transaction.set(null);
		endTransaction(transactionConnection);
		if (exception != null) {
			throw exception;
		}
	}
	
	private Connection beginTransaction() {
		Connection connection = daoDeque.poll();
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

				logger.log(Level.FINE, "Not possible to create additional dao", e);
			}
			// so no dao available and not possible to create one
			// block and wait till a dao is in deque
			try {
				daoDeque.poll(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.log(Level.FINEST, "poll for dao interrupted", e);
			}
		}
	}
	
	private void endTransaction(Connection connection) {
		// last in first out in the hope that recent accessed objects are the fastest
		daoDeque.push(connection);
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
	}

	public boolean isTransactionActive() {
		Connection connection = transaction.get();
		return connection != null;
	}
	
	Connection getConnection() {
		Connection connection = transaction.get();
		if (connection != null) {
			return connection;
		} else {
			return getAutoCommitConnection();
		}
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
			Table<T> table = (Table<T>) getTable(object.getClass());
			return table.insert(object);
		} else {
			return 0;
		}
	}
	
	public <T> void update(T object) {
		Table<T> table = (Table<T>) getTable(object.getClass());
		table.update(object);
	}
	
	public <T> List<Integer> findIds(Class<T> clazz, Object field, Object query) {
		return findIds(clazz, field, query);
	}
	
	public void commit(Connection connection) {
		try {
			connection.commit();
			endTransaction(connection);
		} catch (SQLException x) {
			logger.log(Level.SEVERE, "Could not commit", x);
			throw new RuntimeException("Could not commit");
		}
	}

	public void rollback(Connection connection) {
		try {
			connection.rollback();
			endTransaction(connection);
		} catch (SQLException x) {
			logger.log(Level.SEVERE, "Could not rollback", x);
			throw new RuntimeException("Could not rollback");
		}
	}

	public boolean isDerbyDb() {
		return true;
	}

	public boolean isDerbyMemoryDb() {
		return isDerbyMemoryDb;
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
	
	/**
	 * @param clazz objects of this class will not be inlined
	 * @return
	 */
	public <U> ImmutableTable<U> addImmutableClass(Class<U> clazz) {
		immutables.add(clazz);
		ImmutableTable<U> table = new ImmutableTable<U>(this, clazz);
		tables.put(table.getClazz(), table);
		return table;
	}
	
	public boolean isImmutable(Class<?> clazz) {
		return immutables.contains(clazz);
	}
	
	public void createTables() {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			try {
				table.create();
			} catch (SQLException x) {
				logger.log(Level.SEVERE, "Couldn't initialize table: " + table.getTableName(), x);
				throw new RuntimeException("Couldn't initialize table: " + table.getTableName());
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
