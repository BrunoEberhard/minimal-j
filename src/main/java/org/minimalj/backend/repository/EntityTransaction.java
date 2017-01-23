package org.minimalj.backend.repository;

import org.minimalj.backend.Backend;
import org.minimalj.repository.Repository;
import org.minimalj.transaction.Transaction;

public abstract class EntityTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	@Override
	public RETURN execute() {
		return execute(Backend.getInstance().getRepository());
	}
	
	protected abstract RETURN execute(Repository repository);

	public abstract Class<ENTITY> getEntityClazz();

}
