package org.minimalj.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

public abstract class Authorization {
	private static final Logger logger = Logger.getLogger(Authorization.class.getName());

	private Map<UUID, Subject> userByToken = new HashMap<>();

	public Authorization() {
	}

	private static boolean available = true;
	private static Authorization instance;

	public static Authorization createAuthorization() {
		String userFile = System.getProperty("MjUserFile");
		if (userFile != null) {
			return new PropertiesAuthorization(userFile);
		}

		String authorizationClassName = System.getProperty("MjAuthorization");
		if (!StringUtils.isBlank(authorizationClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Authorization> authorizationClass = (Class<? extends Authorization>) Class.forName(authorizationClassName);
				Authorization authorization = authorizationClass.newInstance();
				return authorization;
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, logger, "Set authorization failed");
			}
		}

		available = false;
		return null;
	}

	public static Authorization getInstance() {
		if (instance == null) {
			instance = createAuthorization();
		}
		return instance;
	}

	public static boolean isAvailable() {
		if (available) {
			getInstance();
		}
		return available;
	}

	protected abstract boolean checkLogin(UserPassword login);

	public Subject login(UserPassword login) {
		if (checkLogin(login)) {
			Subject user = new Subject();
			user.setName(login.user);
			UUID token = UUID.randomUUID();
			user.setToken(token);
			userByToken.put(token, user);
			return user;
		} else {
			return null;
		}
	}

	public void logout(Serializable token) {
		userByToken.remove(token);
	}

	public Subject getUserByToken(Serializable token) {
		return userByToken.get(token);
	}
}
