package org.minimalj.transaction.persistence;

import java.util.List;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.IdUtils;

public class ReadHistoryTransaction<T> implements Transaction<List<T>> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final Object id;
	private final int maxResults;

	@SuppressWarnings("unchecked")
	public ReadHistoryTransaction(T object) {
		this.clazz = (Class<T>) object.getClass();
		this.id = IdUtils.getId(object);
		this.maxResults = Integer.MAX_VALUE;
	}
	
	public ReadHistoryTransaction(Class<T> clazz, Object id) {
		this(clazz, id, Integer.MAX_VALUE);
	}
	
	public ReadHistoryTransaction(Class<T> clazz, Object id, int maxResults) {
		this.clazz = clazz;
		this.id = id;
		this.maxResults = maxResults;
	}

	@Override
	public TransactionType getType() {
		return TransactionType.READ;
	}
	
	@Override
	public Class<?> getClazz() {
		return clazz;
	}

	@Override
	public List<T> execute(Persistence persistence) {
		if (persistence instanceof SqlPersistence) {
			SqlPersistence sqlPersistence = (SqlPersistence) persistence;
			List<T>	result = sqlPersistence.loadHistory(clazz, id, maxResults);
			return result;
		} else {
			throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlPersistence.class.getSimpleName());
		}
	}

}