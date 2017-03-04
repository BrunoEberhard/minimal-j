package org.minimalj.backend;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.backend.repository.CountTransaction;
import org.minimalj.backend.repository.DeleteEntityTransaction;
import org.minimalj.backend.repository.InsertTransaction;
import org.minimalj.backend.repository.ReadCriteriaTransaction;
import org.minimalj.backend.repository.ReadEntityTransaction;
import org.minimalj.backend.repository.SaveTransaction;
import org.minimalj.backend.repository.UpdateTransaction;
import org.minimalj.repository.Repository;
import org.minimalj.repository.TransactionalRepository;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.sql.SqlRepository;
import org.minimalj.security.Authentication;
import org.minimalj.security.Authorization;
import org.minimalj.transaction.Isolation;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.TransactionAnnotations;

/**
 * A Backend is responsible for executing the transactions.
 * It can do this by keeping a database (SqlRepository) or by
 * delegating everything to an other Backend (SocketBackend).<p>
 * 
 * Every Frontend needs a Backend. But a Backend can serve more
 * than one Frontend.<p>
 * 
 * The Backend configuration must be done with system properties.
 * These are handled in the initBackend method. The configuration
 * cannot be changed during the lifetime of an application VM.<p>
 * 
 * The configuration properties:
 * <UL>
 * <LI><code>MjBackendAddress</code> and <code>MjBackendPort</code>: if
 * these two are set the transactions are delegated to a remote
 * SocketBackendServer.</LI>
 * <LI><code>MjBackend</code>: if this property is set it specifies
 * the classname of the Backend.</LI>
 * <LI>If the Backend should run in the same JVM as the Frontend you
 * don't need to set any property</LI>
 * </UL>
 */
public class Backend {
	private static final Logger logger = Logger.getLogger(SqlRepository.class.getName());

	private static Backend instance;
	
	public static Backend create() {
		String backendAddress = Configuration.get("MjBackendAddress");
		String backendPort = Configuration.get("MjBackendPort", "8020");
		if (backendAddress != null) {
			return new SocketBackend(backendAddress, Integer.valueOf(backendPort));
		} 

		if (Configuration.available("MjBackend")) {
			return Configuration.getClazz("MjBackend", Backend.class);
		}
		return new Backend();
	};
	
	private Repository repository = null; 
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
	
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	public Repository getRepository() {
		if (!isInTransaction()) {
			throw new IllegalStateException("Repository may only be accessed from within a " + Transaction.class.getSimpleName());
		}
		if (repository == null) {
			repository = Repository.create();
		}
		return repository;
	}
	
	protected Authentication createAuthentication() {
		return Authentication.create();
	}
	
	public final Authentication getAuthentication() {
		if (authentication == null) {
			if (authenticationActive == null) {
				authentication = createAuthentication();
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

	public static <T> List<T> find(Class<T> clazz, Query query) {
		return execute(new ReadCriteriaTransaction<T>(clazz, query));
	}

	public static <T> long count (Class<T> clazz, Query query) {
		return execute(new CountTransaction<T>(clazz, query));
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
		if (isAuthenticationActive()) {
			Authorization.check(transaction);
		}

		try {
			currentTransaction.set(transaction);
			if (getRepository() instanceof TransactionalRepository) {
				TransactionalRepository transactionalRepository = (TransactionalRepository) getRepository();
				return doExecute(transaction, transactionalRepository);
			} else {
				return transaction.execute();
			}
		} finally {
			currentTransaction.set(null);
		}
	}

	private <T> T doExecute(Transaction<T> transaction, TransactionalRepository transactionalRepository) {
		Isolation.Level isolationLevel = TransactionAnnotations.getIsolation(transaction);
		if (isolationLevel != null) {
			return doExecute(transaction, transactionalRepository, isolationLevel);
		} else {
			return transaction.execute();
		}
	}

	private <T> T doExecute(Transaction<T> transaction, TransactionalRepository transactionalRepository,
			Isolation.Level isolationLevel) {
		T result;
		boolean commit = false;
		try {
			transactionalRepository.startTransaction(isolationLevel.getLevel());
			result = transaction.execute();
			commit = true;
		} finally {
			transactionalRepository.endTransaction(commit);
		}
		return result;
	}
	
	private void init() {
		if (Configuration.available("MjInit")) {
			Transaction<?> init = Configuration.getClazz("MjInit", Transaction.class);
			init.execute();
		}
	}

}