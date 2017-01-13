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
import org.minimalj.persistence.sql.LazyList;
import org.minimalj.persistence.sql.SqlRepository;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * The common interface of all types of persistences. Note that specific implementations
 * can have more methods. See for example the <code>execute</code> methods in SqlRepository
 *
 */
public abstract class Repository {
	private static final Logger logger = Logger.getLogger(Repository.class.getName());

	public static Repository create() {
		String repositoryClassName = Configuration.get("MjRepository");
		if (!StringUtils.isBlank(repositoryClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Repository> repositoryClass = (Class<? extends Repository>) Class.forName(repositoryClassName);
				return repositoryClass.newInstance();
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, logger, "Set repository failed (" + repositoryClassName + ")");
			}
		} 
		
		Class<?>[] entityClasses = Application.getInstance().getEntityClasses();

		DataSource jndiDataSource = getJndiDataSource();
		if (jndiDataSource != null) {
			return new SqlRepository(jndiDataSource, entityClasses);
		}
		
		String database = Configuration.get("MjSqlDatabase");
		String user = Configuration.get("MjSqlDatabaseUser", "APP");
		String password = Configuration.get("MjSqlDatabasePassword", "APP");
		if (StringUtils.isBlank(database)) {
			String databaseFile = Configuration.get("MjSqlDatabaseFile", null);
			boolean createTables = databaseFile == null || !new File(databaseFile).exists();
			return new SqlRepository(SqlRepository.embeddedDataSource(databaseFile), createTables, entityClasses);
		} else {
			return new SqlRepository(SqlRepository.mariaDbDataSource(database, user, password), entityClasses);
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
	
	public abstract <ELEMENT, PARENT> List<ELEMENT> getList(LazyList<PARENT, ELEMENT> list);

	public abstract <ELEMENT, PARENT> ELEMENT add(LazyList<PARENT, ELEMENT> list, ELEMENT element);

	public abstract <ELEMENT, PARENT> void remove(LazyList<PARENT, ELEMENT> list, int position);

}
