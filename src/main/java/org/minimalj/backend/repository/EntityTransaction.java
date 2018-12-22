package org.minimalj.backend.repository;

import org.minimalj.transaction.Transaction;

public abstract class EntityTransaction<ENTITY, RETURN> implements Transaction<RETURN> {
	private static final long serialVersionUID = 1L;

	public abstract Class<ENTITY> getEntityClazz();

}
