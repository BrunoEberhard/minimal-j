package org.minimalj.security;

import org.minimalj.backend.Persistence;
import org.minimalj.security.Authenticated.AbstractAuthenticated;
import org.minimalj.transaction.Transaction;

public class LogoutTransaction extends AbstractAuthenticated implements Transaction<Void> {
	private static final long serialVersionUID = 1L;

	public LogoutTransaction(Subject subject) {
		super(subject);
	}

	@Override
	public Void execute(Persistence persistence) {
		// should be handled on Backend
		throw new IllegalStateException();
	}
}
