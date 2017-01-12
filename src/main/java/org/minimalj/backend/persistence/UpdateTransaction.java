package org.minimalj.backend.persistence;

import org.minimalj.persistence.Repository;

public class UpdateTransaction<ENTITY> extends WriteTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	public UpdateTransaction(ENTITY object) {
		super(object);
	}

	@Override
	public ENTITY execute(Repository repository) {
		repository.update(getUnwrapped());
		return null;
	}
}