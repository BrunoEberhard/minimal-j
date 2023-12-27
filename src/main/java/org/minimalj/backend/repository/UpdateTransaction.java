package org.minimalj.backend.repository;

import org.minimalj.util.IdUtils;

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
	
	@Override
	public String toString() {
		Object id = IdUtils.getId(getUnwrapped()); 
		return "Update " + getUnwrapped().getClass().getSimpleName() + " " + id;
	}
}