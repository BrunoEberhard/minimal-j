package org.minimalj.backend;

import java.util.List;
import java.util.logging.Logger;

import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.security.Authorization;
import org.minimalj.security.IsAuthorizationActive;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.transaction.persistence.DeleteEntityTransaction;
import org.minimalj.transaction.persistence.InsertTransaction;
import org.minimalj.transaction.persistence.ReadCriteriaTransaction;
import org.minimalj.transaction.persistence.ReadEntityTransaction;
import org.minimalj.transaction.persistence.SaveTransaction;
import org.minimalj.transaction.persistence.UpdateTransaction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * A backend is reponsible for executing the transactions.
 * It can do this by keeping a database (SqlBackend) or by
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
public class Backend {
	private static final Logger logger = Logger.getLogger(SqlPersistence.class.getName());

	private static InheritableThreadLocal<Backend> current = new InheritableThreadLocal<Backend>() {
		@Override
		protected Backend initialValue() {
			String backendAddress = System.getProperty("MjBackendAddress");
			String backendPort = System.getProperty("MjBackendPort", "8020");
			if (backendAddress != null) {
				return new SocketBackend(backendAddress, Integer.valueOf(backendPort));
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

			return new Backend();
		};
	};
	
	private Authorization authorization = Authorization.defaultAuthorization; 
	private Persistence persistence = null; 
	
	private Transaction<?> transaction = null;
	
	public static void setInstance(Backend backend) {
		Backend.current.set(backend);
	}

	public static Backend getInstance() {
		return current.get();
	}
	
	public static void setPersistence(Persistence persistence) {
		getInstance().persistence = persistence;
	}
	
	public static Persistence getPersistence() {
		if (!isTransaction()) {
			throw new IllegalStateException("Persistence may only be accessed from within a " + Transaction.class.getSimpleName());
		}
		Backend backend = current.get();
		if (backend.persistence == null) {
			backend.persistence = Persistence.create();
		}
		return backend.persistence;
	}
	
	public static void setAuthorization(Authorization authorization) {
		getInstance().authorization = authorization;
	}
	
	public static Authorization getAuthorization() {
		return getInstance().authorization;
	}
	
	public static boolean isTransaction() {
		Backend backend = current.get();
		return backend != null && backend.transaction != null;
	}
	
	public static boolean isAuthorizationActive() {
		return execute(new IsAuthorizationActive());
	}

	// Could be moved in a class like "Do" but that would seem even more like magic
	// Backend.read or Backend.execute gives a hint that there is a separation between
	// the current stuff and the things done in the Transaction
	
	public static <T> T read(Class<T> clazz, Object id) {
		return execute(new ReadEntityTransaction<T>(clazz, id, null));
	}

	public static <T> List<T> read(Class<T> clazz, Criteria criteria, int maxResults) {
		List<T> result = execute(new ReadCriteriaTransaction<T>(clazz, criteria, maxResults));
		return result;
	}

	public static <T> Object insert(T object) {
		return execute(new InsertTransaction<T>(object));
	}

	public static <T> void update(T object) {
		execute(new UpdateTransaction<T>(object));
	}

	public static <T> T save(T object) {
		return execute(new SaveTransaction<T>(object));
	}

	public static <T> void delete(Class<T> clazz, Object id) {
		execute(new DeleteEntityTransaction<T>(clazz, id));
	}
	
	public static <T> T execute(Transaction<T> transaction) {
		return getInstance().doExecute(transaction);
	}
	
	public <T> T doExecute(Transaction<T> transaction) {
		if (authorization == null || authorization.isAllowed(transaction)) {
			try {
				this.transaction = transaction;
				return transaction.execute();
			} finally {
				this.transaction = null;
			}
		} else {
			throw new IllegalStateException(transaction.getClass().getSimpleName() + " forbidden");
		}
	}
	
}