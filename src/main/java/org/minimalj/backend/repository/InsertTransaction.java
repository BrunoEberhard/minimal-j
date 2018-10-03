package org.minimalj.backend.repository;

public class InsertTransaction<T> extends WriteTransaction<T, Object> {
	private static final long serialVersionUID = 1L;

	public InsertTransaction(T object) {
		super(object);
	}
	
	@Override
	public Object execute() {
		return insert(getUnwrapped());
	}
}