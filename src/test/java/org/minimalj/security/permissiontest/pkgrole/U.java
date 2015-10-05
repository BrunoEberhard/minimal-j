package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;

@Role("transactionRole")
public class U implements Transaction<U> {
	private static final long serialVersionUID = 1L;

	public U() {
	}

	@Override
	public U execute(Persistence persistence) {
		return null;
	}

}
