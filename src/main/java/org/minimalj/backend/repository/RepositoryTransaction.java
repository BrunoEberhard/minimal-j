package org.minimalj.backend.repository;

import org.minimalj.backend.Backend;
import org.minimalj.repository.Repository;
import org.minimalj.transaction.Isolation;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;

public abstract class RepositoryTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	@Override
	public Isolation getIsolation() {
		Isolation isolation = getClass().getAnnotation(Isolation.class);
		if (isolation == null) {
			isolation = getEntityClazz().getAnnotation(Isolation.class);
		}
		return isolation;
	}
	
	@Override
	public Role getRole() {
		Role role = getClass().getAnnotation(Role.class);
		if (role == null) {
			role = getClass().getPackage().getAnnotation(Role.class);
		}
		if (role == null) {
			role = getEntityClazz().getAnnotation(Role.class);
		}
		if (role == null) {
			role = getEntityClazz().getPackage().getAnnotation(Role.class);
		}
		return role;
	}
	
	@Override
	public RETURN execute() {
		return execute(Backend.getInstance().getRepository());
	}
	
	protected abstract RETURN execute(Repository repository);

	public abstract Class<ENTITY> getEntityClazz();

}
