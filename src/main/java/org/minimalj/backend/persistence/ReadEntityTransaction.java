package org.minimalj.backend.persistence;

import org.minimalj.persistence.Persistence;
import org.minimalj.persistence.sql.SqlPersistence;

public class ReadEntityTransaction<ENTITY> extends ReadTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	private final Object id;
	private final Integer time;

	public ReadEntityTransaction(Class<ENTITY> clazz, Object id) {
		this(clazz, id, null);
	}
	
	public ReadEntityTransaction(Class<ENTITY> clazz, Object id, Integer time) {
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