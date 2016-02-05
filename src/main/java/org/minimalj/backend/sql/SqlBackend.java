package org.minimalj.backend.sql;

import java.io.File;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.transaction.Transaction;

public class SqlBackend extends Backend {

	private final SqlPersistence persistence;

	public SqlBackend() {
		String databaseFile = System.getProperty("MjBackendDatabaseFile", null);
		boolean createTables = databaseFile == null || !new File(databaseFile).exists();
		this.persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(databaseFile), createTables, Application.getApplication().getEntityClasses());
	}
	
	public SqlBackend(String database, String user, String password) {
		this.persistence = new SqlPersistence(SqlPersistence.mariaDbDataSource(database, user, password), Application.getApplication().getEntityClasses());
	}
	
	//

	@Override
	public <T> T doExecute(Transaction<T> transaction) {
		if (transaction instanceof PersistenceTransaction) {
			return persistence.execute((PersistenceTransaction<T>) transaction);
		} else {
			return transaction.execute();
		}
	}
	
}
