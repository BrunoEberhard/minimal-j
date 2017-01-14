package org.minimalj.repository;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.repository.criteria.Criteria;
import org.minimalj.repository.sql.LazyList;
import org.minimalj.repository.sql.SqlRepository;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * The common interface of all types of repositories. Note that specific implementations
 * can have more methods. See for example the <code>execute</code> methods in SqlRepository
 *
 */
public interface Repository {
	public static final Logger logger = Logger.getLogger(Repository.class.getName());

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
	
	public static DataSource getJndiDataSource() {
		try {
			Context initContext = new InitialContext();
			DataSource dataSource = (DataSource) initContext.lookup("java:/comp/env/jdbc");
			return dataSource;
		} catch (NamingException e) {
			logger.fine("Exception while retrieving JNDI datasource (" + e.getMessage() + ")");
			return null;
		}
	}
	
	// object handling
	
	public <T> T read(Class<T> clazz, Object id);

	public <T> List<T> read(Class<T> clazz, Criteria criteria, int maxResults);

	public <T> Object insert(T object);

	public <T> void update(T object);

	public <T> void delete(Class<T> clazz, Object id);
	
	// list handling
	
	public <ELEMENT, PARENT> List<ELEMENT> getList(LazyList<PARENT, ELEMENT> list);

	public <ELEMENT, PARENT> ELEMENT add(LazyList<PARENT, ELEMENT> list, ELEMENT element);

	public <ELEMENT, PARENT> void remove(LazyList<PARENT, ELEMENT> list, int position);

}
