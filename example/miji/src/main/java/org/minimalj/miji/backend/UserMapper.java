package org.minimalj.miji.backend;

import java.util.Map;

import org.minimalj.miji.model.User;

public class UserMapper {

	public User map(Object input) {
		User user = new User();
		if (input instanceof Map) {
			Map<Object, Object> map = (Map<Object, Object>) input;
			user.key = (String) map.get("key");
			user.displayName = (String) map.get("displayName");
			
		} else {
			throw new IllegalArgumentException();
		}
		
		return user;
	}

}
