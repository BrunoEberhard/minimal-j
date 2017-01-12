package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.backend.persistence.PersistenceTransaction;
import org.minimalj.persistence.Persistence;
import org.minimalj.transaction.Role;

@Role("transactionRole")
public class TestTransactionU<ENTITY, RETURN> extends PersistenceTransaction<ENTITY, RETURN> {
	private static final long serialVersionUID = 1L;

	public TestTransactionU() {
	}

	@Override
	protected RETURN execute(Persistence persistence) {
		return null;
	}
	
	@Override
	public Class<ENTITY> getEntityClazz() {
		return null;
	}
}

