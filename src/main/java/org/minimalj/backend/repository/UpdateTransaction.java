package org.minimalj.backend.repository;

public class UpdateTransaction<ENTITY> extends WriteTransaction<ENTITY, Void> {
	private static final long serialVersionUID = 1L;

	public UpdateTransaction(ENTITY object) {
		super(object);
	}

	@Override
	public Void execute() {
		update(getUnwrapped());
		return null;
	}
}