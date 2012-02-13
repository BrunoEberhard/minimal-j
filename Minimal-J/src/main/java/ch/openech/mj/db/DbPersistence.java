package ch.openech.mj.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.derby.jdbc.EmbeddedDriver;

public class DbPersistence {
	public static final Logger logger = Logger.getLogger(DbPersistence.class.getName());
	
	public static final String DERBY_DEFAULT_URL = "jdbc:derby:memory:TempDB;create=true";
	// public static final String DERBY_DEFAULT_URL = "jdbc:derby:C:\\Dokumente und Einstellungen\\bruno\\OpenEchDB13;create=true";

	public static final String DERBY_USER = "APP";
	public static final String DERBY_PASSWORD = "APP";

	private boolean initialized = false;
	
	private Connection connection;
	private boolean isDerbyDb;
	private boolean isDerbyMemoryDb;
	private boolean isMySqlDb;
	
	private final Map<Class<?>, AbstractTable<?>> tables = new HashMap<Class<?>, AbstractTable<?>>();
	
	public DbPersistence() throws SQLException {
	}
	
	public void connect() throws SQLException {
		connect(DERBY_DEFAULT_URL, DERBY_USER, DERBY_PASSWORD);
	}
	
	public void connect(String connectionUrl, String user, String password) throws SQLException {
		this.isDerbyDb = connectionUrl.startsWith("jdbc:derby");
		this.isMySqlDb = connectionUrl.startsWith("jdbc:mysql");
		this.isDerbyMemoryDb = connectionUrl.startsWith("jdbc:derby:memory");
		
		if (isDerbyMemoryDb) {
			DriverManager.registerDriver(new EmbeddedDriver());
		}
		
		connection = DriverManager.getConnection(connectionUrl, user, password);
		connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		connection.setAutoCommit(false);
		
		if (!initialized) {
			collectImmutableTables();
			initializeTables();
			initialized = true;
		}
	}
	
	public void disconnect() throws SQLException {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			table.closeStatements();
		}
		
		connection.close();
	}
	
	public void commit() throws SQLException {
		connection.commit();
	}

	public void rollback() throws SQLException {
		connection.rollback();
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
		return connection;
	}
	
	//

	public void add(Table<?> table) throws SQLException {
		if (initialized) {
			throw new IllegalStateException("Not allowed to add Table after connecting");
		}
		tables.put(table.getClazz(), table);
	}
	
	public <U> Table<U> addClass(Class<U> clazz) throws SQLException {
		Table<U> table = new Table<U>(this, clazz);
		add(table);
		return table;
	}

	protected void collectImmutableTables() throws SQLException {
		List<AbstractTable<?>> tableList = new ArrayList<AbstractTable<?>>(tables.values());
		for (AbstractTable<?> table : tableList) {
			table.collectImmutables();
		}
	}
	
	protected void initializeTables() throws SQLException {
		for (AbstractTable<?> table : tables.values()) {
			table.initialize();
		}
		getConnection().commit();
	}

	<U> ImmutableTable<U> addImmutableTable(Class<U> clazz) throws SQLException {
		ImmutableTable<U> table = new ImmutableTable<U>(this, clazz);
		tables.put(clazz, table);
		return table;
	}
	
	@SuppressWarnings("unchecked")
	public <U> ImmutableTable<U> getImmutableTable(Class<U> clazz) throws SQLException {
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
