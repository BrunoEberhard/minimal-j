package org.minimalj.backend;

import org.minimalj.frontend.Frontend;
import org.minimalj.security.Authenticated;
import org.minimalj.security.LoginTransaction;
import org.minimalj.security.LogoutTransaction;
import org.minimalj.transaction.Transaction;

public class AuthenticationBackend extends Backend {

	private final Backend backend;

	public AuthenticationBackend(Backend backend) {
		this.backend = backend;
	}

	@Override
	public Persistence getPersistence() {
		return backend.getPersistence();
	}

	@Override
	public <T> T execute(Transaction<T> transaction) {
		if (!(transaction instanceof LoginTransaction || transaction instanceof LogoutTransaction)) {
			return backend.execute(new Authenticated.AuthenticatedTransaction<T>(transaction, Frontend.getBrowser().getSubject()));
		} else {
			return backend.execute(transaction);
		}
	}

}
