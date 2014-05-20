package org.minimalj.backend.db;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.MjApplication;
import org.minimalj.backend.Backend;
import org.minimalj.model.annotation.ViewOf;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.IdUtils;

public class DbBackend extends Backend {

	private final DbPersistence persistence;

	public DbBackend() {
		// TODO make this configurable
		this.persistence = new DbPersistence(DbPersistence.embeddedDataSource(), MjApplication.getApplication().getEntityClasses());
	}
	
	//
	
	@Override
	public <T extends Serializable> T execute(Transaction<T> transaction) {
		boolean runThrough = false;
		T result;
		try {
			persistence.startTransaction();
			result = transaction.execute(this);
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
			result = streamConsumer.comsume(this, inputStream);
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
			result = streamProducer.consume(this, outputStream);
			runThrough = true;
		} finally {
			persistence.endTransaction(runThrough);
		}
		return result;
	}

	@Override
	public <T> T read(Class<T> clazz, long id) {
		Table<T> table = persistence.getTable(clazz);
		return table.read(id);
	}

	@Override
	public <T> List<T> read(Class<T> resultClass, Criteria criteria, int maxResults) {
		if (ViewOf.class.isAssignableFrom(resultClass)) {
			Class<?> viewedClass = DbPersistenceHelper.getViewedClass(resultClass);
			Table<?> table = persistence.getTable(viewedClass);
			return table.readView(resultClass, criteria, maxResults);
		} else {
			Table<T> table = (Table<T>) persistence.table(resultClass);
			return table.read(criteria, maxResults);
		}
	}

	@Override
	public <T> long insert(T object) {
		return persistence.insert(object);
	}

	@Override
	public <T> void update(T object) {
		persistence.update(object);
	}
	
	@Override
	public <T> void delete(T object) {
		persistence.delete(object);
	}

	@Override
	public <T> void deleteAll(Class<T> clazz) {
		Table<T> table = persistence.getTable(clazz);
		table.clear();
	}

	@Override
	public <T> T read(Class<T> clazz, long id, Integer time) {
		AbstractTable<T> abstractTable = (AbstractTable<T>) persistence.table(clazz);
		if (abstractTable instanceof HistorizedTable) {
			return ((HistorizedTable<T>) abstractTable).read(id, time);
		} else {
			throw new IllegalArgumentException(clazz + " is not historized");
		}
	}

	@Override
	public <T> List<T> loadHistory(T object) {
		@SuppressWarnings("unchecked")
		AbstractTable<T> abstractTable = (AbstractTable<T>) persistence.table(object.getClass());
		if (abstractTable instanceof HistorizedTable) {
			long id = IdUtils.getId(object);
			List<Integer> times = ((HistorizedTable<T>) abstractTable).readVersions(id);
			List<T> result = new ArrayList<>();
			for (int time : times) {	
				result.add(((HistorizedTable<T>) abstractTable).read(id, time));
			}
			return result;
		} else {
			throw new IllegalArgumentException(object.getClass() + " is not historized");
		}
	}

	@Override
	public Serializable executeStatement(String queryName, Serializable... parameters) {
		// TODO
		String query;
		if (queryName.equals("MaxPerson")) {
			query = "SELECT MAX(ID) FROM PERSON";
			persistence.execute(query);
		} else if (queryName.equals("MaxOrganisation")) {
			query = "SELECT MAX(ID) FROM ORGANISATION";
			persistence.execute(query);
		} else if (queryName.equals("DeleteAll")) {
			// TODO
		} else if (queryName.equals("MaxCustomer")) {
			query = "SELECT MAX(ID) FROM CUSTUMER";
		}
		// TODO
		return null;
	}
	
}
