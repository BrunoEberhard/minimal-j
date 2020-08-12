package org.minimalj.backend.repository;

import org.minimalj.security.Authorization;
import org.minimalj.security.Subject;
import org.minimalj.transaction.Transaction;

public abstract class EntityTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	public abstract Class<ENTITY> getEntityClazz();

	@Override
	public boolean hasAccess(Subject subject) {
		Boolean allowed = Authorization.hasAccessByAnnotation(subject, this.getClass());
		if (allowed != null) {
			return allowed;
		}

		return !Boolean.FALSE.equals(Authorization.hasAccessByAnnotation(subject, getEntityClazz()));
	}

}
