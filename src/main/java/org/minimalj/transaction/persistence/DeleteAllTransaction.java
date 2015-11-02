package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;
import org.minimalj.backend.sql.SqlBackend;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.transaction.PersistenceTransaction;

public class DeleteAllTransaction implements PersistenceTransaction<Void> {
	private static final long serialVersionUID = 1L;

	private final String className;
	private transient Class<?> clazz;

	public DeleteAllTransaction(final Class<?> clazz) {
		this.className = clazz.getName();
		this.clazz = clazz;
	}
	
	@Override
	public Class<?> getEntityClazz() {
		if (clazz == null) {
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return clazz;
	}

	@Override
	public Void execute(Persistence persistence) {
		if (persistence instanceof SqlPersistence) {
			SqlPersistence sqlPersistence = (SqlPersistence) persistence;
			sqlPersistence.deleteAll(getEntityClazz());
			return null;
		} else {
			throw new IllegalStateException(getClass().getSimpleName() + " works only with " + SqlBackend.class.getSimpleName());
		}
	}

}