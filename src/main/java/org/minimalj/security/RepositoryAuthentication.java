package org.minimalj.security;

import java.util.List;

import org.minimalj.repository.Repository;
import org.minimalj.repository.criteria.By;
import org.minimalj.security.model.User;

public class RepositoryAuthentication extends UserPasswordAuthentication {
	private static final long serialVersionUID = 1L;

	private final transient Repository authenticationRepository;
	
	public RepositoryAuthentication(Repository authenticationRepository) {
		this.authenticationRepository = authenticationRepository;
	}
	
	protected RepositoryAuthentication() {
		this.authenticationRepository = null;
	}
	
	protected User retrieveUser(String userName) {
		List<User> users =  authenticationRepository.read(User.class, By.field(User.$.name, userName), 1);
		return users.isEmpty() ? null : users.get(0);
	}
}
