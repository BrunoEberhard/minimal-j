package org.minimalj.security;

import java.util.List;

import org.minimalj.persistence.Persistence;
import org.minimalj.persistence.criteria.By;
import org.minimalj.security.model.User;

public class PersistenceAuthentication extends UserPasswordAuthentication {
	private static final long serialVersionUID = 1L;

	private final transient Persistence authenticationPersistence;
	
	public PersistenceAuthentication(Persistence authenticationPersistence) {
		this.authenticationPersistence = authenticationPersistence;
	}
	
	protected PersistenceAuthentication() {
		this.authenticationPersistence = null;
	}
	
	protected User retrieveUser(String userName) {
		List<User> users =  authenticationPersistence.read(User.class, By.field(User.$.name, userName), 1);
		return users.isEmpty() ? null : users.get(0);
	}
}
