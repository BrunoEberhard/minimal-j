package org.minimalj.example.miniboost.security;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.model.Role;
import org.minimalj.example.miniboost.model.User;
import org.minimalj.repository.query.By;
import org.minimalj.security.UserPasswordAuthentication;
import org.minimalj.security.model.UserRole;
import org.minimalj.util.CloneHelper;

public class MiniBoostAuthentication extends UserPasswordAuthentication {
	private static final long serialVersionUID = 1L;

	@Override
	protected org.minimalj.security.model.User retrieveUser(String userName) {
		List<User> userList = Backend.find(User.class, By.field(User.$.loginname, userName).limit(1));
		if (userList.isEmpty()) {
			return null;
		} 
		User user = userList.get(0);
		org.minimalj.security.model.User mjUser = new org.minimalj.security.model.User();
		mjUser.name = user.loginname;
		for (Role role : user.roles) {
			mjUser.roles.add(new UserRole(role.id));
		}
		CloneHelper.deepCopy(user.password, mjUser.password);
		return mjUser;
	}
	
}
