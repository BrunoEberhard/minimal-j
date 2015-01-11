package org.minimalj.transaction.persistence;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.backend.db.DbBackend;
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
	public List<T> execute(Backend backend) {
		if (backend instanceof DbBackend) {
			DbBackend dbBackend = (DbBackend) backend;
			List<T>	result = dbBackend.loadHistory(clazz, id, maxResults);
			return result;
		} else {
			throw new IllegalStateException(getClass().getSimpleName() + " works only with " + DbBackend.class.getSimpleName());
		}
	}

}