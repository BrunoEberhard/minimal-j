package org.minimalj.security;

import java.util.List;

import org.minimalj.persistence.Repository;
import org.minimalj.persistence.criteria.By;
import org.minimalj.security.model.User;

public class PersistenceAuthentication extends UserPasswordAuthentication {
	private static final long serialVersionUID = 1L;

	private final transient Repository authenticationRepository;
	
	public PersistenceAuthentication(Repository authenticationRepository) {
		this.authenticationRepository = authenticationRepository;
	}
	
	protected PersistenceAuthentication() {
		this.authenticationRepository = null;
	}
	
	protected User retrieveUser(String userName) {
		List<User> users =  authenticationRepository.read(User.class, By.field(User.$.name, userName), 1);
		return users.isEmpty() ? null : users.get(0);
	}
}
