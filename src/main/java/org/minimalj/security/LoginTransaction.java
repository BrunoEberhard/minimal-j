package org.minimalj.security;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public class LoginTransaction implements Transaction<MjUser> {
	private static final long serialVersionUID = 1L;
	
	private final Login login;
	
	public LoginTransaction(Login login) {
		this.login = login;
	}
	
	public Login getLogin() {
		return login;
	}
	
	@Override
	public MjUser execute(Persistence persistence) {
		// should be handled on Backend
		throw new IllegalStateException();
	}
}
