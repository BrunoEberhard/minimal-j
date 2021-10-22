package org.minimalj.test;

import org.minimalj.security.UserPasswordAuthentication;
import org.minimalj.security.model.User;

public class TestAuthentication extends UserPasswordAuthentication {
	private static final long serialVersionUID = 1L;

	@Override
	protected User retrieveUser(String userName) {
		if (userName.length() >= 2) {
			User user = new User();
			user.name = userName;
			return user;
		} else {
			return null;
		}
	}

	protected User retrieveUser(String userName, char[] password) {
		return retrieveUser(userName);
	}
}