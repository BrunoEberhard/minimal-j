package org.minimalj.backend;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.backend.db.DbBackend;
import org.minimalj.backend.db.DbPersistence;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * A backend is reponsible for executing the transactions.
 * It can do this by keeping a database (DbBackend) or by
 * delegating everything to an other backend (SocketBackend).<p>
 * 
 * Every frontend needs a backend. But a backend can serve more
 * than one frontend.<p>
 * 
 * The backend configuration must be done with system properties.
 * These are handled in the initBackend method. The configuration
 * cannot be changed during the lifetime of an application VM.<p>
 * 
 * The configuration properties:
 * <UL>
 * <LI><code>MjBackendAddress</code> and <code>MjBackendPort</code>: if
 * these two are set the transactions are delegated to a remote
 * SocketBackendServer.</LI>
 * <LI><code>MjBackendDatabase</code>, <code>MjBackendDatabaseUser</code>, <code>MjBackendDatabasePassword</code>: if
 * these properties are set the transactions are executed with
 * a relational database.</LI>
 * <LI><code>MjBackend</code>: if this property is set it specifies
 * the classname of the backend.</LI>
 * </UL>
 */
public abstract class Backend {
	private static final Logger logger = Logger.getLogger(DbPersistence.class.getName());

	private static Backend instance;
	
	private static void initBackend() {
		String backendAddress = System.getProperty("MjBackendAddress");
		String backendPort = System.getProperty("MjBackendPort", "8020");
		if (backendAddress != null) {
			Backend.setSocketBackend(backendAddress, Integer.valueOf(backendPort));
			return;
		} 

		String database = System.getProperty("MjBackendDatabase");
		String user= System.getProperty("MjBackendDatabaseUser", "APP");
		String password = System.getProperty("MjBackendDatabasePassword", "APP");
		if (!StringUtils.isBlank(database)) {
			Backend.setDbBackend(database, user, password);
			return;
		}
		
		String backendClassName = System.getProperty("MjBackend");
		if (!StringUtils.isBlank(backendClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Backend> backendClass = (Class<? extends Backend>) Class.forName(backendClassName);
				Backend backend = backendClass.newInstance();
				Backend.setInstance(backend);
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, logger, "Set backend failed");
			}
			return;
		} 
		
		Backend.setEmbeddedDbBackend();
	}
	
	public static void setSocketBackend(String backendAddress, int port) {
		setInstance(new SocketBackend(backendAddress, port));
	}

	public static void setEmbeddedDbBackend() {
		setInstance(new DbBackend());
	}
	
	public static void setDbBackend(String database, String user, String password) {
		setInstance(new DbBackend(database, user, password));
	}
	
	public static void setInstance(Backend backend) {
		if (Backend.instance != null) {
			throw new IllegalStateException("Backend cannot be changed");
		}		
		if (backend == null) {
			throw new IllegalArgumentException("Backend cannot be null");
		}
		instance = backend;
	}
	
	public static Backend getInstance() {
		if (instance == null) {
			initBackend();
		}
		return instance;
	}

	public abstract <T> T execute(Transaction<T> transaction);
	public abstract <T extends Serializable> T execute(StreamConsumer<T> streamConsumer, InputStream inputStream);
	public abstract <T extends Serializable> T execute(StreamProducer<T> streamProducer, OutputStream outputStream);

	// persistence
	
	public abstract <T> T read(Class<T> clazz, Object id);
	public abstract <T> List<T> read(Class<T> clazz, Criteria criteria, int maxResults);

	public abstract <T> Object insert(T object);
	public abstract <T> void update(T object);
	public abstract <T> void delete(Class<T> clazz, Object id);

	public abstract <T> T executeStatement(Class<T> clazz, String queryName, Serializable... parameter);
	public abstract <T> List<T> executeStatement(Class<T> clazz, String queryName, int maxResults, Serializable... parameter);
}
