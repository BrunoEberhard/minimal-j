package org.minimalj.backend.db;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.minimalj.application.MjApplication;
import org.minimalj.backend.Backend;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.ViewOf;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.IdUtils;

public class DbBackend extends Backend {

	private final DbPersistence persistence;
	private final Map<String, String> queries;

	public DbBackend() {
		this.persistence = new DbPersistence(DbPersistence.embeddedDataSource(), MjApplication.getApplication().getEntityClasses());
		this.queries = MjApplication.getApplication().getQueries();
	}
	
	public DbBackend(String database, String user, String password) {
		this.persistence = new DbPersistence(DbPersistence.mariaDbDataSource(database, user, password), MjApplication.getApplication().getEntityClasses());
		this.queries = MjApplication.getApplication().getQueries();
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
	public <T> T read(Class<T> clazz, long id) {
		Table<T> table = persistence.getTable(clazz);
		return table.read(id);
	}

	@Override
	public <T> List<T> read(Class<T> resultClass, Criteria criteria, int maxResults) {
		if (ViewOf.class.isAssignableFrom(resultClass)) {
			Class<?> viewedClass = ViewUtil.getViewedClass(resultClass);
			Table<?> table = persistence.getTable(viewedClass);
			return table.readView(resultClass, criteria, maxResults);
		} else {
			Table<T> table = persistence.getTable(resultClass);
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
