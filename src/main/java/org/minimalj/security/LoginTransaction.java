package org.minimalj.security;

import java.util.Objects;

import org.minimalj.transaction.Transaction;

public class LoginTransaction implements Transaction<Subject> {
	private static final long serialVersionUID = 1L;
	
	private final UserPassword userPassword;
	
	public LoginTransaction(UserPassword userPassword) {
		Objects.nonNull(userPassword);
		Objects.nonNull(userPassword.user);
		
		this.userPassword = userPassword;
	}
	
	public UserPassword getLogin() {
		return userPassword;
	}
	
	@Override
	public Subject execute() {
		return Authorization.getCurrent().login(userPassword);
	}
}
