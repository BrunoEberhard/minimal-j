package ch.openech.mj.db;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.backend.Backend;
import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.model.annotation.ViewOf;
import ch.openech.mj.transaction.StreamConsumer;
import ch.openech.mj.transaction.StreamProducer;
import ch.openech.mj.transaction.Transaction;
import ch.openech.mj.util.IdUtils;

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
		Table<T> table = (Table<T>) persistence.getTable(clazz);
		return table.read(id);
	}

	@Override
	public <T> List<T> search(Class<T> resultClass, String query, int maxResults) {
		if (ViewOf.class.isAssignableFrom(resultClass)) {
			Class<?> viewedClass = DbPersistenceHelper.getViewedClass(resultClass);
			return ((Table) persistence.getTable(viewedClass)).search(resultClass, query, maxResults);
		} else {
			return ((Table) persistence.getTable(resultClass)).search(query, maxResults);
		}
	}

	@Override
	public <T> List<T> search(Class<T> resultClass, Object[] fields, String query, int maxResults) {
		if (ViewOf.class.isAssignableFrom(resultClass)) {
			Class<?> viewedClass = DbPersistenceHelper.getViewedClass(resultClass);
			return ((Table) persistence.getTable(viewedClass)).search(resultClass, fields, query, maxResults);
		} else {
			return ((Table) persistence.getTable(resultClass)).search(fields, query, maxResults);
		}
	}

	@Override
	public <T> List<T> read(Class<T> clazz, Criteria criteria) {
		Table<T> table = (Table<T>) persistence.getTable(clazz);
		return table.read(criteria);
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
		Table<T> table = (Table<T>) persistence.getTable(clazz);
		table.clear();
	}

	@Override
	public <T> T read(Class<T> clazz, long id, Integer time) {
		AbstractTable<T> abstractTable = (AbstractTable<T>) persistence.getTable(clazz);
		if (abstractTable instanceof HistorizedTable) {
			return ((HistorizedTable<T>) abstractTable).read(id, time);
		} else {
			throw new IllegalArgumentException(clazz + " is not historized");
		}
	}

	@Override
	public <T> List<T> loadHistory(T object) {
		@SuppressWarnings("unchecked")
		AbstractTable<T> abstractTable = (AbstractTable<T>) persistence.getTable(object.getClass());
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
		}
		// TODO
		return null;
	}
	
}
