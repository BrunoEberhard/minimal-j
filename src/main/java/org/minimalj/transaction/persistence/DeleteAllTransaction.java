package org.minimalj.transaction.persistence;

import org.minimalj.backend.Backend;
import org.minimalj.backend.db.DbBackend;
import org.minimalj.transaction.Transaction;

public class DeleteAllTransaction implements Transaction<Void> {
	private static final long serialVersionUID = 1L;

	private final Class<?> clazz;

	public DeleteAllTransaction(final Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Void execute(Backend backend) {
		if (backend instanceof DbBackend) {
			DbBackend dbBackend = (DbBackend) backend;
			dbBackend.deleteAll(clazz);
			return null;
		} else {
			throw new IllegalStateException(getClass().getSimpleName() + " works only with " + DbBackend.class.getSimpleName());
		}
	}

}