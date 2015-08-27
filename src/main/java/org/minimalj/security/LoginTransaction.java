package org.minimalj.security;

import org.minimalj.backend.Persistence;
import org.minimalj.transaction.Transaction;

public class LoginTransaction implements Transaction<Subject> {
	private static final long serialVersionUID = 1L;
	
	private final UserPassword userPassword;
	
	public LoginTransaction(UserPassword userPassword) {
		this.userPassword = userPassword;
	}
	
	public UserPassword getLogin() {
		return userPassword;
	}
	
	@Override
	public Subject execute(Persistence persistence) {
		return Authorization.getInstance().login(userPassword);
	}
}
