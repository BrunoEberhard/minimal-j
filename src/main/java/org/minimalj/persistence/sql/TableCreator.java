package org.minimalj.persistence.sql;

import javax.sql.DataSource;

import org.minimalj.application.Application;

public class TableCreator {

	public static void main(String[] args) throws Exception {
		Application.initApplication(args);
		Application application = Application.getInstance();
		
		String database = System.getProperty("MjSqlDatabase");
		String user = System.getProperty("MjSqlDatabaseUser", "APP");
		String password = System.getProperty("MjSqlDatabasePassword", "APP");
		
		DataSource dataSource = SqlPersistence.mariaDbDataSource(database, user, password);
		new SqlPersistence(dataSource, SqlPersistence.CREATE_TABLES, application.getEntityClasses());
	}

}
