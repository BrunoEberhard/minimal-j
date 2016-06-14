package org.minimalj.backend;

import java.util.List;
import java.util.logging.Logger;

import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.security.Subject;
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
	
	public static void setInstance(Backend backend) {
		Backend.current.set(backend);
	}

	public static Backend getInstance() {
		return current.get();
	}

	// TODO move static methods to "PersistenceTransaction"
	
	public static <T> T read(Class<T> clazz, Object id) {
		return getInstance().execute(new ReadEntityTransaction<T>(clazz, id, null));
	}

	public static <T> List<T> read(Class<T> clazz, Criteria criteria, int maxResults) {
		List<T> result = getInstance().execute(new ReadCriteriaTransaction<T>(clazz, criteria, maxResults));
		return result;
	}

	public static <T> Object insert(T object) {
		return getInstance().execute(new InsertTransaction<T>(object));
	}

	public static <T> void update(T object) {
		getInstance().execute(new UpdateTransaction<T>(object));
	}

	public static <T> T save(T object) {
		return getInstance().execute(new SaveTransaction<T>(object));
	}

	public static <T> void delete(Class<T> clazz, Object id) {
		getInstance().execute(new DeleteEntityTransaction<T>(clazz, id));
	}
	
	public final <T> T execute(Transaction<T> transaction) {
		if (Subject.hasRoleFor(transaction)) {
			return doExecute(transaction);
		} else {
			throw new IllegalStateException(transaction.getClass().getSimpleName() + " forbidden");
		}
	}
	
	public <T> T doExecute(Transaction<T> transaction) {
		return transaction.execute();
	}
	
}