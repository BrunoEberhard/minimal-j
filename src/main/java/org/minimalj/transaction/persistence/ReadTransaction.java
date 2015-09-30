package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.db.DbBackend;
import org.minimalj.backend.db.DbPersistence;
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
	public T execute(Persistence persistence) {
		T result;
		if (time == null) {
			result = persistence.read(clazz, id);
		} else {
			if (persistence instanceof DbPersistence) {
				DbPersistence dbPersistence = (DbPersistence) persistence;
				result = dbPersistence.readVersion(clazz, id, time);
			} else {
				throw new IllegalStateException(getClass().getSimpleName() + " works only with " + DbBackend.class.getSimpleName());
			}
		}
		return result;
	}

}