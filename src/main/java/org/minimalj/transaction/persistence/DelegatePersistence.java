package org.minimalj.transaction.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

import org.minimalj.backend.Backend;
import org.minimalj.backend.Persistence;

public class DelegatePersistence implements Persistence {

	private final Backend backend;

	public DelegatePersistence(Backend backend) {
		this.backend = backend;
	}

	@Override
	public <T> T read(Class<T> clazz, Object id) {
		return backend.execute(new ReadTransaction<T>(clazz, id, null));
	}

	@Override
	public <T> List<T> read(Class<T> clazz, Predicate<T> predicate, int maxResults) {
		List<T> result = backend.execute(new ReadCriteriaTransaction<T>(clazz, predicate, maxResults));
		return result;
	}

	@Override
	public <T> Object insert(T object) {
		return backend.execute(new InsertTransaction<T>(object));
	}

	@Override
	public <T> T update(T object) {
		return backend.execute(new UpdateTransaction<T>(object));
	}

	@Override
	public <T> void delete(Class<T> clazz, Object id) {
		backend.execute(new DeleteTransaction(clazz, id));
	}

	@Override
	public <T> T execute(Class<T> clazz, String query, Serializable... parameter) {
		ExecuteTransaction<T> executeTransaction = new ExecuteTransaction<T>(clazz, query, parameter);
		return backend.execute(executeTransaction);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> List<T> execute(Class<T> clazz, String query, int maxResults, Serializable... parameter) {
		ExecuteTransaction<T> executeTransaction = new ExecuteTransaction<T>(clazz, query, maxResults, parameter);
		return (List<T>) backend.execute(executeTransaction);
	}

}
