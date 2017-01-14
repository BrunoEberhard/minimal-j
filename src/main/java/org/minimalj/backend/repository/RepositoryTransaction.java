package org.minimalj.backend.repository;

import org.minimalj.backend.Backend;
import org.minimalj.repository.Repository;
import org.minimalj.transaction.Isolation;
import org.minimalj.transaction.Transaction;

public abstract class RepositoryTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	@Override
	public Isolation getIsolation() {
		return getEntityClazz().getAnnotation(Isolation.class);
	}
	
	@Override
	public RETURN execute() {
		return execute(Backend.getInstance().getRepository());
	}
	
	protected abstract RETURN execute(Repository repository);

	public abstract Class<ENTITY> getEntityClazz();

}
