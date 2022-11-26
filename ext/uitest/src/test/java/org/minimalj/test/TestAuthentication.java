package org.minimalj.test;

import org.minimalj.security.UserPasswordAuthentication;
import org.minimalj.security.model.User;
import org.minimalj.security.model.UserData;
import org.minimalj.security.model.UserRole;

public class TestAuthentication extends UserPasswordAuthentication {
	private static final long serialVersionUID = 1L;

	public static final String ROLE_TEST = "TestRole";
	
	@Override
	protected UserData retrieveUser(String userName) {
		if (userName.length() >= 2) {
			User user = new User();
			user.name = userName;
			user.roles.add(new UserRole(ROLE_TEST));
			return user;
		} else {
			return null;
		}
	}

	protected UserData retrieveUser(String userName, char[] password) {
		return retrieveUser(userName);
	}
}