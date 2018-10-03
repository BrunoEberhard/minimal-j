package org.minimalj.security.permissiontest;

import org.minimalj.backend.repository.EntityTransaction;
import org.minimalj.security.permissiontest.pkgrole.TestEntityH;
import org.minimalj.transaction.Role;

@Role("transactionRole")
public class TestTransactionU<RETURN> extends EntityTransaction<TestEntityH, RETURN> {
	private static final long serialVersionUID = 1L;

	public TestTransactionU() {
	}

	@Override
	public RETURN execute() {
		return null;
	}
	
	@Override
	public Class<TestEntityH> getEntityClazz() {
		return TestEntityH.class;
	}
}

