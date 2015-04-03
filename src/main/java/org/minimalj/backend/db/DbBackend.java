package org.minimalj.backend.db;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.IdUtils;

public class DbBackend extends Backend {

	private final DbPersistence persistence;
	private final Map<String, String> queries;

	public DbBackend() {
		this.persistence = new DbPersistence(DbPersistence.embeddedDataSource(), Application.getApplication().getEntityClasses());
		this.queries = Application.getApplication().getQueries();
	}
	
	public DbBackend(String database, String user, String password) {
		this.persistence = new DbPersistence(DbPersistence.mariaDbDataSource(database, user, password), Application.getApplication().getEntityClasses());
		this.queries = Application.getApplication().getQueries();
	}
	
	//
	
	@Override
	public <T> T execute(Transaction<T> transaction) {
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
			result = streamConsumer.consume(this, inputStream);
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
			result = streamProducer.produce(this, outputStream);
			runThrough = true;
		} finally {
			persistence.endTransaction(runThrough);
		}
		return result;
	}

	@Override
	public <T> T read(Class<T> clazz, Object id) {
		Table<T> table = persistence.getTable(clazz);
		return table.read(id);
	}

	@Override
	public <T> List<T> read(Class<T> resultClass, Criteria criteria, int maxResults) {
		if (View.class.isAssignableFrom(resultClass)) {
			Class<?> viewedClass = ViewUtil.getViewedClass(resultClass);
			Table<?> table = persistence.getTable(viewedClass);
			return table.readView(resultClass, criteria, maxResults);
		} else {
			Table<T> table = persistence.getTable(resultClass);
			return table.read(criteria, maxResults);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T insert(T object) {
		persistence.insert(object);
		return (T) read(object.getClass(), IdUtils.getId(object));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T update(T object) {
		persistence.update(object);
		return (T) read(object.getClass(), IdUtils.getId(object));
	}
	
	@Override
	public <T> void delete(Class<T> clazz, Object id) {
		persistence.delete(clazz, id);
	}

	public <T> void deleteAll(Class<T> clazz) {
		Table<T> table = persistence.getTable(clazz);
		table.clear();
	}

	public <T> T read(Class<T> clazz, Object id, Integer time) {
		AbstractTable<T> abstractTable = (AbstractTable<T>) persistence.table(clazz);
		if (abstractTable instanceof HistorizedTable) {
			return ((HistorizedTable<T>) abstractTable).read(id, time);
		} else {
			throw new IllegalArgumentException(clazz + " is not historized");
		}
	}

	public <T> List<T> loadHistory(Class<?> clazz, Object id, int maxResult) {
		// TODO maxResults is ignored in loadHistory
		@SuppressWarnings("unchecked")
		AbstractTable<T> abstractTable = (AbstractTable<T>) persistence.table(clazz);
		if (abstractTable instanceof HistorizedTable) {
			List<Integer> times = ((HistorizedTable<T>) abstractTable).readVersions(id);
			List<T> result = new ArrayList<>();
			for (int time : times) {	
				result.add(((HistorizedTable<T>) abstractTable).read(id, time));
			}
			return result;
		} else {
			throw new IllegalArgumentException(clazz.getSimpleName() + " is not historized");
		}
	}
	
	@Override
	public <T> T executeStatement(Class<T> clazz, String queryName, Serializable... parameter) {
		if (queries == null || !queries.containsKey(queryName)) {
			throw new RuntimeException("Query not available: " + queryName);
		}
		T result = null;
		String query = queries.get(queryName);
		result = persistence.execute(clazz, query);
		return result;
	}
	
	@Override
	public <T> List<T> executeStatement(Class<T> clazz, String queryName, int maxResults, Serializable... parameters) {
		return persistence.execute(clazz, queryName, maxResults, parameters);
	}
	
}
