package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.PersistenceTransaction;

public class T implements PersistenceTransaction<T> {
	private static final long serialVersionUID = 1L;

	public T() {
	}

	@Override
	public T execute(Persistence persistence) {
		return null;
	}

}
