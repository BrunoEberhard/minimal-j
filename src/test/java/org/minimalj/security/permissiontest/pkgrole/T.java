package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.PersistenceTransaction;

public class T<ENTITY, RETURN> extends PersistenceTransaction<ENTITY, RETURN> {
	private static final long serialVersionUID = 1L;

	public T() {
	}

	@Override
	protected RETURN execute(Persistence persistence) {
		return null;
	}
}
