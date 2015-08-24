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

	private Map<UUID, MjUser> userByAuthentication = new HashMap<>();

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

	protected abstract boolean checkLogin(Login login);

	public MjUser login(Login login) {
		if (checkLogin(login)) {
			MjUser user = new MjUser();
			user.setName(login.user);
			UUID authentication = UUID.randomUUID();
			user.setAuthentication(authentication);
			userByAuthentication.put(authentication, user);
			return user;
		} else {
			return null;
		}
	}

	public void logout(Serializable authentication) {
		userByAuthentication.remove(authentication);
	}

	public MjUser getUserByAuthentication(Serializable authentication) {
		return userByAuthentication.get(authentication);
	}
}
