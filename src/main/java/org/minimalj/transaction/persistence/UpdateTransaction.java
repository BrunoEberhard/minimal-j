package org.minimalj.transaction.persistence;

import org.minimalj.backend.Persistence;

public class UpdateTransaction<ENTITY> extends ObjectPersistenceTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	public UpdateTransaction(ENTITY object) {
		super(object);
	}

	@Override
	public ENTITY execute(Persistence persistence) {
		persistence.update(getUnwrapped());
		return null;
	}
}