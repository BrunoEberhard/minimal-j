package org.minimalj.security;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public class LogoutTransaction implements Transaction<Void> {
	private static final long serialVersionUID = 1L;

	public LogoutTransaction() {
	}

	@Override
	public Void execute(Persistence persistence) {
		Authorization.getInstance().logout();
		return null;
	}

}
