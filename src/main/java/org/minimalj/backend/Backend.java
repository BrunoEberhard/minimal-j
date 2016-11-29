package org.minimalj.backend;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.backend.persistence.DeleteEntityTransaction;
import org.minimalj.backend.persistence.InsertTransaction;
import org.minimalj.backend.persistence.ReadCriteriaTransaction;
import org.minimalj.backend.persistence.ReadEntityTransaction;
import org.minimalj.backend.persistence.SaveTransaction;
import org.minimalj.backend.persistence.UpdateTransaction;
import org.minimalj.persistence.Persistence;
import org.minimalj.persistence.criteria.Criteria;
import org.minimalj.persistence.sql.SqlPersistence;
import org.minimalj.security.Authentication;
import org.minimalj.security.Authorization;
import org.minimalj.transaction.Transaction;
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
 * <LI><code>MjBackend</code>: if this property is set it specifies
 * the classname of the backend.</LI>
 * <LI>If the backend should run in the same JVM as the frontend you
 * don't need to set any property</LI>
 * </UL>
 */
public class Backend {
	private static final Logger logger = Logger.getLogger(SqlPersistence.class.getName());

	private static Backend instance;
	
	public static Backend create() {
		String backendAddress = Configuration.get("MjBackendAddress");
		String backendPort = Configuration.get("MjBackendPort", "8020");
		if (backendAddress != null) {
			return new SocketBackend(backendAddress, Integer.valueOf(backendPort));
		} 

		String backendClassName = Configuration.get("MjBackend");
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
	
	private Persistence persistence = null; 
	private Boolean authenticationActive = null;
	private Authentication authentication = null; 
	
	private InheritableThreadLocal<Transaction<?>> currentTransaction = new InheritableThreadLocal<>();
	
	public static void setInstance(Backend instance) {
		Objects.nonNull(instance);
		if (Backend.instance != null) {
			throw new IllegalStateException("Not allowed to change instance of " + Backend.class.getSimpleName());
		}
		Backend.instance = instance;
		instance.init();
	}

	public static Backend getInstance() {
		if (instance == null) {
			setInstance(create());
		}
		return instance;
	}
	
	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}
	
	public Persistence getPersistence() {
		if (!isInTransaction()) {
			throw new IllegalStateException("Persistence may only be accessed from within a " + Transaction.class.getSimpleName());
		}
		if (persistence == null) {
			persistence = Persistence.create();
		}
		return persistence;
	}
	
	protected Authentication createAuthentication() {
		return Authentication.create();
	}
	
	public final Authentication getAuthentication() {
		if (authentication == null) {
			if (authenticationActive == null) {
				authentication = Authentication.create();
				authenticationActive = authentication != null;
			}
		}
		return authentication;
	}
	
	public boolean isAuthenticationActive() {
		return getAuthentication() != null;
	}
	
	public boolean isInTransaction() {
		return currentTransaction.get() != null;
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
		if (Authorization.isAllowed(transaction)) {
			try {
				currentTransaction.set(transaction);
				return transaction.execute();
			} finally {
				currentTransaction.set(null);
			}
		} else {
			throw new IllegalStateException(transaction.getClass().getSimpleName() + " forbidden");
		}
	}
	
	private void init() {
		String initClassName = Configuration.get("MjInit");
		if (initClassName != null) {
			try {
				Class<?> initClass = Class.forName(initClassName);
				Object init = initClass.newInstance();
				if (init instanceof Transaction) {
					logger.info("Execute initialization: " + initClassName);
					((Transaction<?>) init).execute();
				} else {
					logger.severe("Class " + initClassName + " doesn't extend Transaction");
				}
			} catch (ClassNotFoundException e) {
				logger.severe("Could not found initialization class: " + initClassName);
			} catch (InstantiationException | IllegalAccessException e) {
				logger.log(Level.SEVERE, "Could not instantiate initialization class: " + initClassName, e);
			}
		}
	}

}