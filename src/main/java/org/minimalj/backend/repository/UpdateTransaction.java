package org.minimalj.backend.repository;

public class UpdateTransaction<ENTITY> extends WriteTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	public UpdateTransaction(ENTITY object) {
		super(object);
	}

	@Override
	public ENTITY execute() {
		update(getUnwrapped());
		return null;
	}
}