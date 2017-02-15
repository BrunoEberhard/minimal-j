package org.minimalj.security;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.repository.Repository;
import org.minimalj.repository.criteria.By;
import org.minimalj.security.model.User;

/**
 * This Authentication holds user, passwords and roles in the applications
 * repository. You have to add the User - class to your Application.getEntityClasses
 * to let the Repository handle the needed classes.
 *
 */
public class RepositoryAuthentication extends UserPasswordAuthentication {
	private static final long serialVersionUID = 1L;

	private final transient Repository authenticationRepository;
	
	public RepositoryAuthentication() {
		this(Backend.getInstance().getRepository());
	}
	
	protected RepositoryAuthentication(Repository authenticationRepository) {
		this.authenticationRepository = authenticationRepository;
	}
	
	protected User retrieveUser(String userName) {
		List<User> users =  authenticationRepository.find(User.class, By.field(User.$.name, userName));
		return users.isEmpty() ? null : users.get(0);
	}
}
