package org.minimalj.backend.persistence;

import org.minimalj.persistence.Persistence;
import org.minimalj.persistence.sql.SqlPersistence;

public class DeleteAllTransaction<ENTITY> extends DeleteEntityTransaction<ENTITY> {
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