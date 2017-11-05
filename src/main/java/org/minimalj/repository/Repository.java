package org.minimalj.repository;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.minimalj.application.Configuration;
import org.minimalj.model.Model;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.sql.SqlRepository;
import org.minimalj.util.StringUtils;

/**
 * The common interface of all types of repositories. Note that specific implementations
 * can have more methods. See for example the <code>execute</code> methods in SqlRepository
 *
 */
public interface Repository {
	public static final Logger logger = Logger.getLogger(Repository.class.getName());

	public static Repository create(Model model) {
		if (Configuration.available("MjRepository")) {
			return Configuration.getClazz("MjRepository", Repository.class, model);
		}

		DataSource jndiDataSource = DataSourceFactory.getJndiDataSource();
		if (jndiDataSource != null) {
			return new SqlRepository(jndiDataSource, model);
		}
		
		String database = Configuration.get("MjSqlDatabase");
		String user = Configuration.get("MjSqlDatabaseUser", "APP");
		String password = Configuration.get("MjSqlDatabasePassword", "APP");
		
		if (!StringUtils.isBlank(database)) {
			return new SqlRepository(DataSourceFactory.dataSource(database, user, password), model);
		} else {
			String databaseFile = Configuration.get("MjSqlDatabaseFile", null);
			boolean createTables = databaseFile == null || !new File(databaseFile).exists();
			return new SqlRepository(DataSourceFactory.embeddedDataSource(databaseFile), createTables, model);
		}
	}
	
	// 
	
	public <T> T read(Class<T> clazz, Object id);

	public <T> List<T> find(Class<T> clazz, Query query);
	
	public <T> long count(Class<T> clazz, Query query);

	public <T> Object insert(T object);

	public <T> void update(T object);

	public <T> void delete(Class<T> clazz, Object id);

}
