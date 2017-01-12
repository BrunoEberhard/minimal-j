package org.minimalj.backend.persistence;

import org.minimalj.persistence.Repository;

public class InsertTransaction<T> extends WriteTransaction<T, Object> {
	private static final long serialVersionUID = 1L;

	public InsertTransaction(T object) {
		super(object);
	}
	
	@Override
	public Object execute(Repository repository) {
		return repository.insert(getUnwrapped());
	}
}