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
		boolean runThrough = false;
		T result;
		if (transaction instanceof PersistenceTransaction) {
			if (persistence.isTransactionActive()) {
				result = ((PersistenceTransaction<T>) transaction).execute(persistence);
			} else {
				try {
					persistence.startTransaction();
					result = ((PersistenceTransaction<T>) transaction).execute(persistence);
					runThrough = true;
				} finally {
					persistence.endTransaction(runThrough);
				}
			}
		} else {
			result = transaction.execute();
		}
		return result;
	}
	
}
