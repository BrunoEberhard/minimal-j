package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.SqlPersistence;

public class DeleteAllTransaction<ENTITY> extends ClassPersistenceTransaction<ENTITY, Void> {
	private static final long serialVersionUID = 1L;

	public DeleteAllTransaction(final Class<ENTITY> clazz) {
		super(clazz);
	}
	
	@Override
	public Void execute(Persistence persistence) {
		if (persistence instanceof SqlPersistence) {
			SqlPersistence sqlPersistence = (SqlPersistence) persistence;
			sqlPersistence.deleteAll(getEntityClazz());
			return null;
		} else {
			throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlPersistence.class.getSimpleName());
		}
	}

}