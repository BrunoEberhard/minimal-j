package org.minimalj.persistence.sql;

import javax.sql.DataSource;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.model.test.ModelTest;

public class TableCreator {

	public static void main(String[] args) throws Exception {
		Application.initApplication(args);
		Application application = Application.getInstance();
		
		ModelTest.exitIfProblems();
		
		String database = Configuration.get("MjSqlDatabase");
		String user = Configuration.get("MjSqlDatabaseUser", "APP");
		String password = Configuration.get("MjSqlDatabasePassword", "APP");
		
		DataSource dataSource = SqlPersistence.mariaDbDataSource(database, user, password);
		new SqlPersistence(dataSource, SqlPersistence.CREATE_TABLES, application.getEntityClasses());
	}

}
