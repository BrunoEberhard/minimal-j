package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.backend.persistence.PersistenceTransaction;
import org.minimalj.persistence.Repository;

public class TestTransaction<ENTITY, RETURN> extends PersistenceTransaction<ENTITY, RETURN> {
	private static final long serialVersionUID = 1L;

	public TestTransaction() {
	}

	@Override
	protected RETURN execute(Repository repository) {
		return null;
	}
	
	@Override
	public Class<ENTITY> getEntityClazz() {
		return null;
	}
}
