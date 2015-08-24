package org.minimalj.transaction.persistence;

import java.io.Serializable;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.backend.Persistence;
import org.minimalj.transaction.criteria.Criteria;

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
	public <T> List<T> read(Class<T> clazz, Criteria criteria, int maxResults) {
		List<T> result = backend.execute(new ReadCriteriaTransaction<T>(clazz, criteria, maxResults));
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> T executeStatement(Class<T> clazz, String queryName, Serializable... parameter) {
		StatementTransaction statementTransaction = new StatementTransaction(clazz, queryName, parameter);
		return (T) backend.execute(statementTransaction);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> List<T> executeStatement(Class<T> clazz, String queryName, int maxResults, Serializable... parameter) {
		StatementTransaction statementTransaction = new StatementTransaction(clazz, queryName, maxResults, parameter);
		return (List<T>) backend.execute(statementTransaction);
	}

}
