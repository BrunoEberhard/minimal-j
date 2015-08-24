package org.minimalj.security;

import org.minimalj.backend.Persistence;
import org.minimalj.security.Authenticated.AbstractAuthenticated;
import org.minimalj.transaction.Transaction;

public class LogoutTransaction extends AbstractAuthenticated implements Transaction<Void> {
	private static final long serialVersionUID = 1L;

	public LogoutTransaction(MjUser user) {
		super(user.getAuthentication());
	}

	@Override
	public Void execute(Persistence persistence) {
		// should be handled on Backend
		throw new IllegalStateException();
	}
}
