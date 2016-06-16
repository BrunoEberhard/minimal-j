package org.minimalj.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Authorization {
	private static ThreadLocal<Serializable> securityToken = new ThreadLocal<>();
	private Map<UUID, Subject> userByToken = new HashMap<>();

	private static boolean active = true;

	private static InheritableThreadLocal<Authorization> current = new InheritableThreadLocal<Authorization>() {
		@Override
		protected Authorization initialValue() {
			if (!active) {
				return null;
			}
			
			String userFile = System.getProperty("MjUserFile");
			if (userFile != null) {
				return new TextFileAuthorization(userFile);
			}
	
			String jaasConfiguration = System.getProperty("MjJaasConfiguration");
			if (jaasConfiguration != null) {
				return new JaasAuthorization(jaasConfiguration);
			}
	
			active = false;
			return null;
		}
	};

	public static void setCurrent(Authorization instance) {
		if (current.get() != null) {
			throw new IllegalStateException("Cannot change authorization instance");
		}
		current.set(instance);
	}
	
	public static Authorization getCurrent() {
		return current.get();
	}
	
	public static boolean isActive() {
		getCurrent();
		return active;
	}
	
	/**
	 * @return null if login failed, empty list if user has no roles
	 */
	protected abstract List<String> retrieveRoles(UserPassword userPassword);

	/**
	 * 
	 * @return null if login failed, empty list if user has no roles
	 */
	public Subject login(UserPassword userPassword) {
		List<String> roles = retrieveRoles(userPassword);
		if (roles != null) {
			Subject subject = new Subject();
			subject.setName(userPassword.user);
			subject.getRoles().addAll(roles);
			UUID token = UUID.randomUUID();
			subject.setToken(token);
			userByToken.put(token, subject);
			return subject;
		} else {
			return null;
		}
	}

	public void logout() {
		userByToken.remove(securityToken.get());
	}

	public Subject getUserByToken(Serializable token) {
		return userByToken.get(token);
	}
	
}