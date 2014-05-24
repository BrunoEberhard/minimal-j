package org.minimalj.backend.db;

import javax.sql.DataSource;

import org.minimalj.application.Launcher;
import org.minimalj.application.MjApplication;

public class GenerateTablesLauncher {

	public static void main(String[] args) throws Exception {
		Launcher.initApplication(args);
		MjApplication application = MjApplication.getApplication();
		
		String database = System.getProperty("MjBackendDatabase");
		String user= System.getProperty("MjBackendDataBaseUser", "APP");
		String password = System.getProperty("MjBackendDataBasePassword", "APP");
		
		DataSource dataSource = DbPersistence.mariaDbDataSource(database, user, password);
		new DbPersistence(dataSource, DbPersistence.CREATE_TABLES, application.getEntityClasses());
	}

}
