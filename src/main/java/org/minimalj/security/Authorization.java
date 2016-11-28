package org.minimalj.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.model.annotation.Grant;
import org.minimalj.model.annotation.Grant.Privilege;
import org.minimalj.persistence.Persistence;
import org.minimalj.security.model.UserPassword;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

public abstract class Authorization {
	private static final Logger LOG = Logger.getLogger(Persistence.class.getName());

	private Map<UUID, Subject> subjectByToken = new HashMap<>();

	public static Authorization instance;
	
	private static boolean active = true;
	
	public static Authorization create() {
		if (!active) {
			return null;
		}
		
		String userFile = Configuration.get("MjUserFile");
		if (userFile != null) {
			return new TextFileAuthorization(userFile);
		}
		
		String authorizationClassName = Configuration.get("MjAuthorization");
		if (!StringUtils.isBlank(authorizationClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Authorization> authorizationClass = (Class<? extends Authorization>) Class.forName(authorizationClassName);
				Authorization authorization = authorizationClass.newInstance();
				return authorization;
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, LOG, "Set authorization failed");
			}
		}

		active = false;
		return null;
	}

	public static void setInstance(Authorization instance) {
		if (Authorization.instance != null) {
			throw new IllegalStateException("Cannot change authorization instance");
		}
		Authorization.instance = instance;
	}
	
	public static Authorization getInstance() {
		if (instance == null) {
			instance = create();
		}
		return instance;
	}
	
	public static boolean isActive() {
		getInstance();
		return active;
	}
	
	public boolean isAllowed(Transaction<?> transaction) {
		Role role = getRole(transaction);
		if (role != null) {
			List<String> currentRoles = getCurrentRoles();
			for (String allowingRole : role.value()) {
				if (currentRoles.contains(allowingRole)) {
					return true;
				}
			}
			return false;
		} else {
			// Transaction specifies no needed role. Every user passes
			return true;
		}
		
	}
	
	public static Role getRole(Transaction<?> transaction) {
		Role role = transaction.getClass().getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		role = transaction.getClass().getPackage().getAnnotation(Role.class);
		return role;
	}
	
	public static void checkGrants(Grant.Privilege privilege, Class<?> clazz) {
		List<String> currentRoles = getCurrentRoles();
		@SuppressWarnings("unused")
		boolean allowed = false;
		Grant[] grantsOnClass = clazz.getAnnotationsByType(Grant.class);
		allowed |= isGranted(currentRoles, privilege, clazz, grantsOnClass);
		Grant[] grantsOnPackage = clazz.getPackage().getAnnotationsByType(Grant.class);
		allowed |= isGranted(currentRoles, privilege, clazz, grantsOnPackage);
		allowed |= isGranted(currentRoles, Privilege.ALL, clazz, grantsOnClass);
		allowed |= isGranted(currentRoles, Privilege.ALL, clazz, grantsOnPackage);
	}
	
	protected static boolean isGranted(List<String> currentRoles, Grant.Privilege privilege, Class<?> clazz, Grant[] grants) {
		if (grants != null) {
			for (Grant grant : grants) {
				if (grant.privilege() == privilege) {
					for (String roleGranted : grant.value()) {
						if (currentRoles.contains(roleGranted)) {
							return true;
						}
					}
					throw new IllegalStateException(privilege + " not allowed on " + clazz.getSimpleName());
				}
			}
		}
		return false;
	}

	protected static List<String> getCurrentRoles() {
		Subject subject = Subject.getCurrent();
		return subject != null ? subject.getRoles() : Collections.emptyList();
	}
	
	public Subject createSubject(String name) {
		Subject subject = new Subject();
		subject.setName(name);
		UUID token = UUID.randomUUID();
		subject.setToken(token);
		subjectByToken.put(token, subject);
		return subject;
	}

	/**
	 * An implementation can decide to provide more methods than just login,
	 * for example for a requested SMS. <code>createSubject</code> should be used
	 * to create and register the returned <code>Subject</code>.
	 * 
	 * @param userPassword input by user
	 * @return Subject created with createSubject method and filled with state and roles
	 */
	protected abstract Subject login(UserPassword userPassword);

	public void logout() {
		subjectByToken.remove(Subject.getCurrent().getToken());
	}

	public Subject getUserByToken(Serializable token) {
		return subjectByToken.get(token);
	}
	
}