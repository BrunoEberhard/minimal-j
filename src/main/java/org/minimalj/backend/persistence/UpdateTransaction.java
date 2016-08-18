package org.minimalj.backend.persistence;

import org.minimalj.persistence.Persistence;

public class UpdateTransaction<ENTITY> extends WriteTransaction<ENTITY, ENTITY> {
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