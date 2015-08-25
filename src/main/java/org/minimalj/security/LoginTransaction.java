package org.minimalj.security;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public class LoginTransaction implements Transaction<Subject> {
	private static final long serialVersionUID = 1L;
	
	private final UserPassword login;
	
	public LoginTransaction(UserPassword login) {
		this.login = login;
	}
	
	public UserPassword getLogin() {
		return login;
	}
	
	@Override
	public Subject execute(Persistence persistence) {
		// should be handled on Backend
		throw new IllegalStateException();
	}
}
