package org.minimalj.repository;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.sql.LazyList;
import org.minimalj.repository.sql.SqlRepository;
import org.minimalj.util.StringUtils;

/**
 * The common interface of all types of repositories. Note that specific implementations
 * can have more methods. See for example the <code>execute</code> methods in SqlRepository
 *
 */
public interface Repository {
	public static final Logger logger = Logger.getLogger(Repository.class.getName());

	public static Repository create() {
		if (Configuration.available("MjRepository")) {
			return Configuration.getClazz("MjRepository", Repository.class);
		}
		
		Class<?>[] entityClasses = Application.getInstance().getEntityClasses();

		DataSource jndiDataSource = DataSourceFactory.getJndiDataSource();
		if (jndiDataSource != null) {
			return new SqlRepository(jndiDataSource, entityClasses);
		}
		
		String database = Configuration.get("MjSqlDatabase");
		String user = Configuration.get("MjSqlDatabaseUser", "APP");
		String password = Configuration.get("MjSqlDatabasePassword", "APP");
		
		if (!StringUtils.isBlank(database)) {
			return new SqlRepository(DataSourceFactory.dataSource(database, user, password), entityClasses);
		} else {
			String databaseFile = Configuration.get("MjSqlDatabaseFile", null);
			boolean createTables = databaseFile == null || !new File(databaseFile).exists();
			return new SqlRepository(DataSourceFactory.embeddedDataSource(databaseFile), createTables, entityClasses);
		}
	}
	
	// object handling
	
	// spring: findOne
	public <T> T read(Class<T> clazz, Object id);

	// spring: find
	public <T> List<T> find(Class<T> clazz, Query query);

	public <T> Object insert(T object);

	public <T> void update(T object);

	public <T> void delete(Class<T> clazz, Object id);
	
	// lazy list handling
	
	public <ELEMENT, PARENT> List<ELEMENT> getList(LazyList<PARENT, ELEMENT> list);

}
