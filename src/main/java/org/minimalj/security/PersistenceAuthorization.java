package org.minimalj.security;

import java.util.List;
import java.util.stream.Collectors;

import org.minimalj.persistence.Persistence;
import org.minimalj.persistence.criteria.By;
import org.minimalj.security.model.User;
import org.minimalj.security.model.UserPassword;

public class PersistenceAuthorization extends Authorization {

	private final Persistence authorizationPersistence;
	
	public PersistenceAuthorization(Persistence authorizationPersistence) {
		this.authorizationPersistence = authorizationPersistence;
	}
	
	protected PersistenceAuthorization() {
		this.authorizationPersistence = null;
	}
	
	@Override
	protected Subject login(UserPassword userPassword) {
		List<User> userList = retrieveUsers(userPassword.user);
		if (userList == null || userList.isEmpty()) {
			return null;
		}
		User user = userList.get(0);
		if (!user.password.validatePassword(userPassword.password)) {
			return null;
		}
		Subject subject = createSubject(userPassword.user);
		List<String> roleNames = user.roles.stream().map((role) -> role.name).collect(Collectors.toList());
		subject.getRoles().addAll(roleNames);
		return subject;
	}

	protected List<User> retrieveUsers(String userName) {
		return authorizationPersistence.read(User.class, By.field(User.$.name, userName), 1);
	}
}
