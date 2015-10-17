package org.minimalj.security;

import org.minimalj.transaction.Transaction;

public class LoginTransaction implements Transaction<Subject> {
	private static final long serialVersionUID = 1L;
	
	private final UserPassword userPassword;
	
	public LoginTransaction() {
		this.userPassword = new UserPassword();
		userPassword.user = "Anonymous";
		userPassword.password = new char[0];
	}
	
	public LoginTransaction(UserPassword userPassword) {
		this.userPassword = userPassword;
	}
	
	public UserPassword getLogin() {
		return userPassword;
	}
	
	@Override
	public Subject execute() {
		if (Authorization.isAvailable()) {
			return Authorization.getInstance().login(userPassword);
		} else {
			return null;
		}
	}
}
