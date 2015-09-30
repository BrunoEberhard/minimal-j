package org.minimalj.backend.sql;

import javax.sql.DataSource;

import org.minimalj.application.Application;

public class TableCreator {

	public static void main(String[] args) throws Exception {
		Application.initApplication(args);
		Application application = Application.getApplication();
		
		String database = System.getProperty("MjBackendDatabase");
		String user= System.getProperty("MjBackendDataBaseUser", "APP");
		String password = System.getProperty("MjBackendDataBasePassword", "APP");
		
		DataSource dataSource = SqlPersistence.mariaDbDataSource(database, user, password);
		new SqlPersistence(dataSource, SqlPersistence.CREATE_TABLES, application.getEntityClasses());
	}

}
