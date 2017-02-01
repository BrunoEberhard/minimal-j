package org.minimalj.security.permissiontest;

import org.minimalj.backend.repository.EntityTransaction;
import org.minimalj.repository.Repository;
import org.minimalj.security.permissiontest.pkgrole.TestEntityH;
import org.minimalj.transaction.Role;

@Role("transactionRole")
public class TestTransactionU<RETURN> extends EntityTransaction<TestEntityH, RETURN> {
	private static final long serialVersionUID = 1L;

	public TestTransactionU() {
	}

	@Override
	protected RETURN execute(Repository repository) {
		return null;
	}
	
	@Override
	public Class<TestEntityH> getEntityClazz() {
		return TestEntityH.class;
	}
}

