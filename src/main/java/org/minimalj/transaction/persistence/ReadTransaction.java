package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.SqlBackend;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.transaction.PersistenceTransaction;

public class ReadTransaction<T> implements PersistenceTransaction<T> {
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
	public Class<?> getEntityClazz() {
		return clazz;
	}

	@Override
	public T execute(Persistence persistence) {
		T result;
		if (time == null) {
			result = persistence.read(clazz, id);
		} else {
			if (persistence instanceof SqlPersistence) {
				SqlPersistence sqlPersistence = (SqlPersistence) persistence;
				result = sqlPersistence.readVersion(clazz, id, time);
			} else {
				throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlBackend.class.getSimpleName());
			}
		}
		return result;
	}

}