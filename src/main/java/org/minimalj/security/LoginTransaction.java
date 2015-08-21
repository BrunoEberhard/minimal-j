package org.minimalj.security;

import org.minimalj.backend.Backend;
import org.minimalj.transaction.Transaction;

public class LoginTransaction implements Transaction<MjUser> {
	private static final long serialVersionUID = 1L;
	
	private final Login login;
	
	public LoginTransaction(Login login) {
		this.login = login;
	}
	
	@Override
	public MjUser execute(Backend backend) {
		MjUser user = new MjUser();
		user.setName(login.user);
		user.getRoles().add("normal");
		if ("su".equals(login.user)) {
			user.getRoles().add("su");
		}
		if (login.password instanceof char[]) {
			System.out.println("chars: " + String.valueOf((char[])login.password));
		} else if (login.password instanceof String) {
			System.out.println("String: " + login.password);
		}
		return user;
	}
}
