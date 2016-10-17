package org.minimalj.persistence;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.persistence.criteria.Criteria;
import org.minimalj.persistence.sql.SqlPersistence;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * The common interface of all types of persistences. Note that specific implementations
 * can have more methods. See for example the <code>execute</code> methods in SqlPersistence
 *
 */
public abstract class Persistence {
	private static final Logger logger = Logger.getLogger(Persistence.class.getName());

	public static Persistence create() {
		String persistenceClassName = Configuration.get("MjPersistence");
		if (!StringUtils.isBlank(persistenceClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Persistence> persistenceClass = (Class<? extends Persistence>) Class.forName(persistenceClassName);
				Persistence persistence = persistenceClass.newInstance();
				return persistence;
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, logger, "Set persistence failed (" + persistenceClassName + ")");
			}
		} 
		
		Class<?>[] entityClasses = Application.getInstance().getEntityClasses();

		DataSource jndiDataSource = getJndiDataSource();
		if (jndiDataSource != null) {
			return new SqlPersistence(jndiDataSource, entityClasses);
		}
		
		String database = Configuration.get("MjSqlDatabase");
		String user = Configuration.get("MjSqlDatabaseUser", "APP");
		String password = Configuration.get("MjSqlDatabasePassword", "APP");
		if (StringUtils.isBlank(database)) {
			String databaseFile = Configuration.get("MjSqlDatabaseFile", null);
			boolean createTables = databaseFile == null || !new File(databaseFile).exists();
			return new SqlPersistence(SqlPersistence.embeddedDataSource(databaseFile), createTables, entityClasses);
		} else {
			return new SqlPersistence(SqlPersistence.mariaDbDataSource(database, user, password), entityClasses);
		}
	}
	
	private static DataSource getJndiDataSource() {
		try {
			Context initContext = new InitialContext();
			DataSource dataSource = (DataSource) initContext.lookup("java:/comp/env/jdbc");
			return dataSource;
		} catch (NamingException e) {
			logger.fine("Exception while retrieving JNDI datasource (" + e.getMessage() + ")");
			return null;
		}
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
