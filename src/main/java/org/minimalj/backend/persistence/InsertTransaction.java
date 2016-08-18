package org.minimalj.backend.persistence;

import org.minimalj.persistence.Persistence;

public class InsertTransaction<T> extends WriteTransaction<T, Object> {
	private static final long serialVersionUID = 1L;

	public InsertTransaction(T object) {
		super(object);
	}
	
	@Override
	public Object execute(Persistence persistence) {
		return persistence.insert(getUnwrapped());
	}
}