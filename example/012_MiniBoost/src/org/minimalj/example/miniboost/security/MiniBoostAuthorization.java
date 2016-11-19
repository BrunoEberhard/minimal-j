package org.minimalj.example.miniboost.security;

import java.util.List;
import java.util.stream.Collectors;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.model.User;
import org.minimalj.persistence.criteria.By;
import org.minimalj.security.Authorization;
import org.minimalj.security.Subject;
import org.minimalj.security.model.UserPassword;

public class MiniBoostAuthorization extends Authorization {

	@Override
	protected Subject login(UserPassword userPassword) {
		List<User> userList = Backend.read(User.class, By.field(User.$.loginname, userPassword.user), 1);
		if (userList.isEmpty()) {
			return null;
		} 
		User user = userList.get(0);
		if (!user.password.validatePassword(userPassword.password)) {
			return null;
		}
		Subject subject = createSubject(userPassword.user);
		List<String> roleNames = user.roles.stream().map((role) -> role.id).collect(Collectors.toList());
		subject.getRoles().addAll(roleNames);
		return subject;
	}
	
}
