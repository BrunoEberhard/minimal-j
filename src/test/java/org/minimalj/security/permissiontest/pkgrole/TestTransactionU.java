package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.backend.repository.EntityTransaction;
import org.minimalj.repository.Repository;
import org.minimalj.transaction.Role;

@Role("transactionRole")
public class TestTransactionU<ENTITY, RETURN> extends EntityTransaction<ENTITY, RETURN> {
	private static final long serialVersionUID = 1L;

	public TestTransactionU() {
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
