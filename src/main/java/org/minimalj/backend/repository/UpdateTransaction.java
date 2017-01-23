package org.minimalj.backend.repository;

import org.minimalj.repository.Repository;

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