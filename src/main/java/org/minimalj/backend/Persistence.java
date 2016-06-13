package org.minimalj.backend;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * The common interface of all types of persistences. Note that specific implementations
 * can have more methods. See for example the <code>execute</code> methods in SqlPersistence
 *
 */
public abstract class Persistence {
	private static final Logger logger = Logger.getLogger(Persistence.class.getName());

	private static Persistence instance;
	
	private static Persistence createPersistence() {
		String persistenceClassName = System.getProperty("MjPersistence");
		if (!StringUtils.isBlank(persistenceClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Persistence> persistenceClass = (Class<? extends Persistence>) Class.forName(persistenceClassName);
				Persistence persistence = persistenceClass.newInstance();
				return persistence;
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, logger, "Set backend failed");
			}
		} 
		
		String database = System.getProperty("MjSqlDatabase");
		String user = System.getProperty("MjSqlDatabaseUser", "APP");
		String password = System.getProperty("MjSqlDatabasePassword", "APP");
		Class<?>[] entityClasses = Application.getApplication().getEntityClasses();
		if (StringUtils.isBlank(database)) {
			String databaseFile = System.getProperty("MjSqlDatabaseFile", null);
			boolean createTables = databaseFile == null || !new File(databaseFile).exists();
			return new SqlPersistence(SqlPersistence.embeddedDataSource(databaseFile), createTables, entityClasses);
		} else {
			return new SqlPersistence(SqlPersistence.mariaDbDataSource(database, user, password), entityClasses);
		}
	}

	public static Persistence getInstance() {
		if (instance == null) {
			instance = createPersistence();
		}
		return instance;
	}
	
	// transaction

	public abstract void startTransaction(int transactionIsolationLevel);
	
	public abstract void endTransaction(boolean commit);
	
	
	// object handling
	
	public abstract <T> T read(Class<T> clazz, Object id);

	public abstract <T> List<T> read(Class<T> clazz, Criteria criteria, int maxResults);

	public abstract <T> Object insert(T object);

	public abstract <T> void update(T object);

	public abstract <T> void delete(Class<T> clazz, Object id);
	
	// list handling, list name is the name of the property containing the list
	
	public abstract <ELEMENT> List<ELEMENT> getList(String listName, Object parentId);

	public abstract <ELEMENT> ELEMENT add(String listName, Object parentId, ELEMENT element);

	public abstract void remove(String listName, Object parentId, int position);

}
