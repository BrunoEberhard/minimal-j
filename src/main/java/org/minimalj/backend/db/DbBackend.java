package org.minimalj.backend.db;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.backend.Persistence;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
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
	
	@Override
	public <T extends Serializable> T execute(StreamConsumer<T> streamConsumer, InputStream inputStream) {
		boolean runThrough = false;
		T result;
		try {
			persistence.startTransaction();
			result = streamConsumer.consume(persistence, inputStream);
			runThrough = true;
		} finally {
			persistence.endTransaction(runThrough);
		}
		return result;
	}

	@Override
	public <T extends Serializable> T execute(StreamProducer<T> streamProducer, OutputStream outputStream) {
		boolean runThrough = false;
		T result;
		try {
			persistence.startTransaction();
			result = streamProducer.produce(persistence, outputStream);
			runThrough = true;
		} finally {
			persistence.endTransaction(runThrough);
		}
		return result;
	}
	
}
