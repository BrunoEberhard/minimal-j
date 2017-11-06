package org.minimalj.backend.repository;

import org.minimalj.repository.Repository;

public class ReadEntityTransaction<ENTITY> extends ReadTransaction<ENTITY, ENTITY> {
	private static final long serialVersionUID = 1L;

	private final Object id;

	public ReadEntityTransaction(Class<ENTITY> clazz, Object id) {
		super(clazz);
		this.id = id;
	}

	@Override
	protected ENTITY execute(Repository repository) {
		ENTITY result = repository.read(getEntityClazz(), id);
		return result;
	}

}