package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public class T implements Transaction<T> {
	private static final long serialVersionUID = 1L;

	public T() {
	}

	@Override
	public T execute(Persistence persistence) {
		return null;
	}

}
