package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.SqlBackend;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.transaction.Transaction;

public class DeleteAllTransaction implements Transaction<Void> {
	private static final long serialVersionUID = 1L;

	private final Class<?> clazz;

	public DeleteAllTransaction(final Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Void execute(Persistence persistence) {
		if (persistence instanceof SqlPersistence) {
			SqlPersistence sqlPersistence = (SqlPersistence) persistence;
			sqlPersistence.deleteAll(clazz);
			return null;
		} else {
			throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlBackend.class.getSimpleName());
		}
	}

}