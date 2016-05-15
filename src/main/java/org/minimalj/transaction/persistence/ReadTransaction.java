package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.SqlPersistence;

public class ReadTransaction<ENTITY> extends ClassPersistenceTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	private final Object id;
	private final Integer time;

	public ReadTransaction(Class<ENTITY> clazz, Object id) {
		this(clazz, id, null);
	}
	
	public ReadTransaction(Class<ENTITY> clazz, Object id, Integer time) {
		super(clazz);
		this.id = id;
		this.time = time;
	}

	@Override
	protected ENTITY execute(Persistence persistence) {
		ENTITY result;
		if (time == null) {
			result = persistence.read(getEntityClazz(), id);
		} else {
			if (persistence instanceof SqlPersistence) {
				SqlPersistence sqlPersistence = (SqlPersistence) persistence;
				result = sqlPersistence.readVersion(getEntityClazz(), id, time);
			} else {
				throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlPersistence.class.getSimpleName());
			}
		}
		return result;
	}

}