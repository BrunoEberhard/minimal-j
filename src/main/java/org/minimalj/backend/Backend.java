package org.minimalj.backend;

import java.util.logging.Logger;

import org.minimalj.backend.db.DbBackend;
import org.minimalj.backend.db.DbPersistence;
import org.minimalj.frontend.Frontend;
import org.minimalj.security.Authorization;
import org.minimalj.security.AuthorizationBackend;
import org.minimalj.transaction.Transaction;
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
	
	public static Backend createBackend() {
		String backendAddress = System.getProperty("MjBackendAddress");
		String backendPort = System.getProperty("MjBackendPort", "8020");
		if (backendAddress != null) {
			return new SocketBackend(backendAddress, Integer.valueOf(backendPort));
		} 

		String database = System.getProperty("MjBackendDatabase");
		String user = System.getProperty("MjBackendDatabaseUser", "APP");
		String password = System.getProperty("MjBackendDatabasePassword", "APP");
		if (!StringUtils.isBlank(database)) {
			return new DbBackend(database, user, password);
		}
		
		String backendClassName = System.getProperty("MjBackend");
		if (!StringUtils.isBlank(backendClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Backend> backendClass = (Class<? extends Backend>) Class.forName(backendClassName);
				Backend backend = backendClass.newInstance();
				return backend;
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, logger, "Set backend failed");
			}
		} 
		
		return new DbBackend();
	}

	public static Backend getInstance() {
		if (instance == null) {
			instance = createBackend();
			if (Frontend.isAvailable()) {
				instance = new AuthenticationBackend(instance);
			} 
			if (Authorization.isAvailable()) {
				instance = new AuthorizationBackend(instance);
			}
		}
		return instance;
	}

	public abstract Persistence getPersistence();
	
	public static Persistence persistence() {
		return getInstance().getPersistence();
	}
	
	public abstract <T> T execute(Transaction<T> transaction);
	
}
