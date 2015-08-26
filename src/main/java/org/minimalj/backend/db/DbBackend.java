package org.minimalj.backend.db;

import java.io.File;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public class DbBackend extends Backend {

	private final DbPersistence persistence;

	public DbBackend() {
		String databaseFile = System.getProperty("MjBackendDatabaseFile", null);
		boolean createTables = databaseFile == null || !new File(databaseFile).exists();
		this.persistence = new DbPersistence(DbPersistence.embeddedDataSource(databaseFile), createTables, Application.getApplication().getEntityClasses());
	}
	
	public DbBackend(String database, String user, String password) {
		this.persistence = new DbPersistence(DbPersistence.mariaDbDataSource(database, user, password), Application.getApplication().getEntityClasses());
	}
	
	@Override
	public Persistence getPersistence() {
		return persistence;
	}
	
	//

	@Override
	public <T> T execute(Transaction<T> transaction) {
		boolean runThrough = false;
		T result;
		try {
			persistence.startTransaction();
			result = transaction.execute(persistence);
			runThrough = true;
		} finally {
			persistence.endTransaction(runThrough);
		}
		return result;
	}
	
}
