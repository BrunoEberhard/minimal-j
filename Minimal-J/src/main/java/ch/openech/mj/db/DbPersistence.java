package ch.openech.mj.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.openech.mj.model.test.ModelTest;

public class DbPersistence {
	public static final Logger logger = Logger.getLogger(DbPersistence.class.getName());
	
	public static final String DEFAULT_URL = "jdbc:derby:memory:TempDB;create=true";
	// public static final String DEFAULT_URL = "jdbc:derby:C:\\Dokumente und Einstellungen\\bruno\\OpenEchDB13;create=true";
	// public static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/openech?user=APP&password=APP"; 

	
	public static final String USER = "APP";
	public static final String PASSWORD = "APP";

	private boolean initialized = false;
	
	private Connection connection;
	private boolean isDerbyDb;
	private boolean isDerbyMemoryDb;
	private boolean isMySqlDb;
	
	private final Map<Class<?>, AbstractTable<?>> tables = new LinkedHashMap<Class<?>, AbstractTable<?>>();
	
	public DbPersistence() {
	}
	
	public void connect() {
		testModel();
		connect(DEFAULT_URL, USER, PASSWORD);
	}

	private void testModel() {
		ModelTest test = new ModelTest();
		for (Class<?> c : tables.keySet()) {
			test.test(c);
		}
		if (!test.getProblems().isEmpty()) {
			for (String s : test.getProblems()) {
				logger.severe(s);
			}
			throw new IllegalArgumentException();
		}
	}
	
	public void connect(String connectionUrl, String user, String password) {
		try {
			connectToCloudFoundry();
		} catch (Exception x) {
			// There is normally no exception if not on cloudfoundry, only the connection stays null
			logger.log(Level.SEVERE, "Exception whe try to connect to CloudFoundry", x);
		}
			
		if (connection == null) {
			try {
				this.isDerbyDb = connectionUrl.startsWith("jdbc:derby");
				this.isMySqlDb = connectionUrl.startsWith("jdbc:mysql");
				this.isDerbyMemoryDb = connectionUrl.startsWith("jdbc:derby:memory");
				
				if (isDerbyMemoryDb) {
					DriverManager.registerDriver(new EmbeddedDriver());
				} else if (isMySqlDb) {
					DriverManager.registerDriver(new com.mysql.jdbc.Driver());
				}
				
				connection = DriverManager.getConnection(connectionUrl, user, password);
	
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				connection.setAutoCommit(false);
			} catch (SQLException x) {
				logger.log(Level.SEVERE, "Could not establish connection to " + connectionUrl);
				throw new RuntimeException("Could not establish connection to " + connectionUrl);
			}
		}
		
		if (!initialized) {
			initializeTables();
			initialized = true;
		}
	}
	
	private void connectToCloudFoundry() throws ClassNotFoundException, SQLException, JSONException {
		String vcap_services = System.getenv("VCAP_SERVICES");
		
		if (vcap_services != null && vcap_services.length() > 0) {
			JSONObject root = new JSONObject(vcap_services);

			JSONArray mysqlNode = (JSONArray) root.get("mysql-5.1");
			JSONObject firstSqlNode = (JSONObject) mysqlNode.get(0);
			JSONObject credentials = (JSONObject) firstSqlNode.get("credentials");

			String dbname = credentials.getString("name");
			String hostname = credentials.getString("hostname");
			String user = credentials.getString("user");
			String password = credentials.getString("password");
			String port = credentials.getString("port");
			
			String dbUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbname;

			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(dbUrl, user, password);
			
			isDerbyDb = false;
			isDerbyMemoryDb = false;
			isMySqlDb = true;
		}
	}

	public void disconnect() {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		boolean firstFail = true;
		for (AbstractTable<?> table : tableList) {
			try {
				table.closeStatements();
			} catch (SQLException x) {
				if (firstFail) {
					logger.log(Level.SEVERE, "Could not close statements of table " + table.name, x);
					firstFail = false;
				} else {
					logger.log(Level.SEVERE, "Could not close statements of table " + table.name);
				}
			}
		}
		
		try {
			connection.close();
		} catch (SQLException x) {
			logger.log(Level.SEVERE, "Could not close connection", x);
			throw new RuntimeException("Could not close connection");
		}
	}
	
	public void clear() {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			if (!(table instanceof ImmutableTable)) {
				table.clear();
			}
		}
	}
	
	public void commit() {
		try {
			connection.commit();
		} catch (SQLException x) {
			logger.log(Level.SEVERE, "Could not commit", x);
			throw new RuntimeException("Could not commit");
		}
	}

	public void rollback() {
		try {
			connection.rollback();
		} catch (SQLException x) {
			logger.log(Level.SEVERE, "Could not rollback", x);
			throw new RuntimeException("Could not rollback");
		}
	}

	public boolean isDerbyDb() {
		return isDerbyDb;
	}

	public boolean isDerbyMemoryDb() {
		return isDerbyMemoryDb;
	}

	public boolean isMySqlDb() {
		return isMySqlDb;
	}
	
	protected Connection getConnection() {
		boolean connected = connection != null;
		try {
			connected &= !connection.isClosed();
		} catch (SQLException x) {
			logger.log(Level.WARNING, "Couldn't check connection", x);
		}
		
		if (!connected) {
			connect();
		}
		return connection;
	}
	
	//

	public void add(Table<?> table) {
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

	<U> ImmutableTable<U> addImmutableClass(Class<U> clazz) {
		ImmutableTable<U> table = new ImmutableTable<U>(this, clazz);
		tables.put(table.getClazz(), table);
		return table;
	}
	
	protected void initializeTables() {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			try {
				table.initialize();
			} catch (SQLException x) {
				logger.log(Level.SEVERE, "Couldn't initialize table: " + table.getTableName(), x);
				throw new RuntimeException("Couldn't initialize table: " + table.getTableName());
			}
		}
		commit();
	}

	@SuppressWarnings("unchecked")
	public <U> ImmutableTable<U> getImmutableTable(Class<U> clazz) {
		return (ImmutableTable<U>) tables.get(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <U> AbstractTable<U> getTable(Class<U> clazz) {
		return (AbstractTable<U>) tables.get(clazz);
	}
	
	public boolean isValidDbVersion() {
		// TODO 
		// int actualVersion = DbVersion.getVersionOf(getConnection());
		return true;
	}
	
}
