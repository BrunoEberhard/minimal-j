package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;

public class InsertTransaction<T> extends ObjectPersistenceTransaction<T, Object> {
	private static final long serialVersionUID = 1L;

	public InsertTransaction(T object) {
		super(object);
	}
	
	@Override
	public Object execute(Persistence persistence) {
		return persistence.insert(getUnwrapped());
	}
}