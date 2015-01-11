package org.minimalj.transaction.persistence;

import org.minimalj.backend.Backend;
import org.minimalj.backend.db.DbBackend;
import org.minimalj.transaction.Transaction;

public class ReadTransaction<T> implements Transaction<T> {
	private static final long serialVersionUID = 1L;

	private final Class<T> clazz;
	private final Object id;
	private final Integer time;

	public ReadTransaction(Class<T> clazz, Object id) {
		this(clazz, id, null);
	}
	
	public ReadTransaction(Class<T> clazz, Object id, Integer time) {
		this.clazz = clazz;
		this.id = id;
		this.time = time;
	}

	@Override
	public T execute(Backend backend) {
		T result;
		if (time == null) {
			result = backend.read(clazz, id);
		} else {
			if (backend instanceof DbBackend) {
				DbBackend dbBackend = (DbBackend) backend;
				result = dbBackend.read(clazz, id, time);
			} else {
				throw new IllegalStateException(getClass().getSimpleName() + " works only with " + DbBackend.class.getSimpleName());
			}
		}
		return result;
	}

}