package org.minimalj.backend;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.repository.CountTransaction;
import org.minimalj.backend.repository.DeleteEntityTransaction;
import org.minimalj.backend.repository.DeleteTransaction;
import org.minimalj.backend.repository.EntityTransaction;
import org.minimalj.backend.repository.InsertTransaction;
import org.minimalj.backend.repository.ReadCriteriaTransaction;
import org.minimalj.backend.repository.ReadEntityTransaction;
import org.minimalj.backend.repository.ReadOneTransaction;
import org.minimalj.backend.repository.SaveTransaction;
import org.minimalj.backend.repository.UpdateTransaction;
import org.minimalj.backend.repository.WriteTransaction;
import org.minimalj.repository.Repository;
import org.minimalj.repository.TransactionalRepository;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Query;
import org.minimalj.security.Authentication;
import org.minimalj.security.Subject;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.Codes;

/**
 * A Backend is responsible for executing the transactions.
 * It can do this by keeping a database (SqlRepository) or by
 * delegating everything to an other Backend (SocketBackend).<p>
 * 
 * Every Frontend needs a Backend. But a Backend can serve more
 * than one Frontend.<p>
 * 
 * The Backend keeps a repository that may only be accessed within
 * a transaction. See EntityTransaction.<p>
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
	private TransactionLogger transactionLogger = new DefaultTransactionLogger();
	
	private InheritableThreadLocal<Transaction<?>> currentTransaction = new InheritableThreadLocal<>();
	
	public static void setInstance(Backend instance) {
		Objects.requireNonNull(instance);
		if (Backend.instance != null) {
			throw new IllegalStateException("Not allowed to change instance of " + Backend.class.getSimpleName());
		}
		Backend.instance = instance;
		Application.getInstance().initBackend();
	}
	
	private static synchronized void createInstance() {
		if (instance == null) {
			setInstance(create());
		}
	}

	public static Backend getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}
	
	protected void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	/**
	 * The backend repository may only be accessed within a transaction. You
	 * must <b>not</b> call getRepository from somewhere else or you will get an
	 * IllegalStateException. To simply read or write an entity use the static
	 * read or insert methods.
	 * 
	 * @return the main/backend repository
	 * @throws IllegalStateException
	 *             if currently no transaction is active
	 */
	public Repository getRepository() {
		if (!isInTransaction()) {
			throw new IllegalStateException("Repository may only be accessed from within a " + Transaction.class.getSimpleName());
		}
		if (repository == null) {
			synchronized (this) {
				if (repository == null) {
					repository = Application.getInstance().createRepository();
				}				
			}
		}
		return repository;
	}
	
	protected Authentication createAuthentication() {
		return Application.getInstance().createAuthentication();
	}
	
	public Authentication getAuthentication() {
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
	
	// These methods are shortcuts for CRUD - Transactions.
	// note: if they are called within a transaction a nested
	// transaction is created.
	
	public static <T> T read(Class<T> clazz, Object id) {
		return execute(new ReadEntityTransaction<>(clazz, id));
	}

	public static <T> List<T> find(Class<T> clazz, Query query) {
		return execute(new ReadCriteriaTransaction<>(clazz, query));
	}
	
	public static <T> T findOne(Class<T> clazz, Query query) {
		return execute(new ReadOneTransaction<>(clazz, query, true));
	}

	public static <T> T findFirst(Class<T> clazz, Query query) {
		return execute(new ReadOneTransaction<>(clazz, query, false));
	}

	public static <T> long count(Class<T> clazz, Criteria criteria) {
		return execute(new CountTransaction<>(clazz, criteria));
	}
	
	public static <T> Object insert(T object) {
		return execute(new InsertTransaction<>(object));
	}

	public static <T> void update(T object) {
		execute(new UpdateTransaction<>(object));
	}

	public static <T> T save(T object) {
		return execute(new SaveTransaction<>(object));
	}

	public static <T> int delete(Class<T> clazz, Criteria criteria) {
		return execute(new DeleteTransaction<>(clazz, criteria));
	}

	public static <T> void delete(T object) {
		execute(new DeleteEntityTransaction<>(object));
	}

	public static <T> T execute(Transaction<T> transaction) {
		return getInstance().doExecute(transaction);
	}
	
	public static <T> T execute(Transaction<T> transaction, boolean propagate) {
		if (propagate && getInstance().isInTransaction()) {
			return transaction.execute();
		} else {
			return execute(transaction);
		}
	}
	
	public <T> T doExecute(Transaction<T> transaction) {
		if (isAuthenticationActive()) {
			if (!transaction.hasAccess(Subject.getCurrent())) {
				throw new IllegalStateException(transaction + " forbidden");
			}
		}

		Transaction<?> outerTransaction = currentTransaction.get();
		try {
			transactionLogger.logStart(transaction);
			currentTransaction.set(transaction);
			T result;
			if (getRepository() instanceof TransactionalRepository) {
				TransactionalRepository transactionalRepository = (TransactionalRepository) getRepository();
				result = doExecute(transaction, transactionalRepository);
			} else {
				result = transaction.execute();
				transactionLogger.logEnd(transaction, result, null);
			}
			handleCodeCache(transaction);
			Application.getInstance().transactionCompleted(transaction);
			return result;
		} finally {
			currentTransaction.set(outerTransaction);
		}
	}

	protected <T> void handleCodeCache(Transaction<T> transaction) {
		if (transaction instanceof WriteTransaction || transaction instanceof DeleteEntityTransaction) {
			// we could check if the transaction is about a code class. But the
			// invalidateCodeCache method is probably faster than to call 'isCode'
			Codes.invalidateCodeCache(((EntityTransaction<?, ?>) transaction).getEntityClazz());
		}
	}

	private <T> T doExecute(Transaction<T> transaction, TransactionalRepository transactionalRepository) {
		T result = null;
		boolean commit = false;
		try {
			transactionalRepository.startTransaction(transaction.getIsolation().getLevel());
			result = transaction.execute();
			commit = true;
		} finally {
			transactionalRepository.endTransaction(commit);
			transactionLogger.logEnd(transaction, result, commit);
		}
		return result;
	}
	
	public interface TransactionLogger {
		
		public void logStart(Transaction<?> transaction);

		public void logEnd(Transaction<?> transaction, Object result, Boolean commit);
	}
	
	public static class DefaultTransactionLogger implements TransactionLogger {
		private static final Logger LOG = Logger.getLogger("Transaction");

		@Override
		public void logStart(Transaction<?> transaction) {
			LOG.log(Level.FINER, () -> "start  " + transaction.toString());
		}

		@Override
		public void logEnd(Transaction<?> transaction, Object result, Boolean commit) {
			String verb = commit != null ? (commit ? "commit" : "ROLLBACK") : "end   ";
			LOG.log(Level.FINER, () -> verb + " " + transaction.toString());
			if ((commit == null || commit) /* && GenericUtils.getTypeArgument(transaction.getClass(), Transaction.class) != Void.class */) {
				LOG.log(Level.FINEST, () -> "Result: " + result);
			}
		}
	}

}