package org.minimalj.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Authorization {
	private static ThreadLocal<Serializable> securityToken = new ThreadLocal<>();
	private Map<UUID, Subject> userByToken = new HashMap<>();

	private static boolean available = true;

	private static InheritableThreadLocal<Authorization> current = new InheritableThreadLocal<Authorization>() {
		@Override
		protected Authorization initialValue() {
			if (!available) {
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
	
			available = false;
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
	
	public static boolean isAvailable() {
		getCurrent();
		return available;
	}
	
	protected abstract List<String> retrieveRoles(UserPassword login);

	public Subject login(UserPassword login) {
		List<String> roles = retrieveRoles(login);
		Subject subject = new Subject();
		if (roles != null) {
			subject.setName(login.user);
			subject.getRoles().addAll(roles);
			UUID token = UUID.randomUUID();
			subject.setToken(token);
			userByToken.put(token, subject);
		}
		return subject;
	}

	public void logout() {
		userByToken.remove(securityToken.get());
	}

	public Subject getUserByToken(Serializable token) {
		return userByToken.get(token);
	}
	
	public static class LoginFailedException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		public LoginFailedException() {
			super("Login failed");
		}
	}
	
}