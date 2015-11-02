package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;

public class UpdateTransaction<T> extends BasePersistenceTransaction<T> {
	private static final long serialVersionUID = 1L;

	public UpdateTransaction(T object) {
		super(object);
	}

	@Override
	public T execute(Persistence persistence) {
		persistence.update(getUnwrapped());
		return null;
	}
}