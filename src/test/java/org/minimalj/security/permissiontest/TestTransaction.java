package org.minimalj.security.permissiontest;

import org.minimalj.backend.repository.EntityTransaction;
import org.minimalj.security.permissiontest.pkgrole.TestEntityH;

public class TestTransaction<RETURN> extends EntityTransaction<TestEntityH, RETURN> {
	private static final long serialVersionUID = 1L;

	public TestTransaction() {
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
