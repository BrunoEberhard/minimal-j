package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.PersistenceTransaction;
import org.minimalj.transaction.Role;

@Role("transactionRole")
public class U implements PersistenceTransaction<U> {
	private static final long serialVersionUID = 1L;

	public U() {
	}

	@Override
	public U execute(Persistence persistence) {
		return null;
	}

}
