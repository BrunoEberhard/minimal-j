package org.minimalj.repository.sql;

import javax.sql.DataSource;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.model.test.ModelTest;
import org.minimalj.repository.DataSourceFactory;

public class TableCreator {

	public static void main(String[] args) throws Exception {
		Application.initApplication(args);
		Application application = Application.getInstance();
		
		ModelTest.exitIfProblems();
		
		String database = Configuration.get("MjSqlDatabase");
		String user = Configuration.get("MjSqlDatabaseUser", "APP");
		String password = Configuration.get("MjSqlDatabasePassword", "APP");
		
		DataSource dataSource = DataSourceFactory.mariaDbDataSource(database, user, password);
		new SqlRepository(dataSource, SqlRepository.CREATE_TABLES, application.getEntityClasses());
	}

}
