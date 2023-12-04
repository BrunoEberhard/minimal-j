package org.minimalj.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.security.model.UserData;
import org.minimalj.transaction.Role;

public final class Subject implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final InheritableThreadLocal<Subject> subject = new InheritableThreadLocal<>();
	private final UserData user;
	private final Object id;
	private final String name;
	private final Serializable token;
	private final List<String> roles;

	/**
	 * Only for tests
	 * 
	 * @param name name
	 */
	public Subject(String name) {
		this.user = null;
		this.id = null;
		this.name = name;
		this.token = null;
		this.roles = Collections.emptyList();
	}

	public Subject(UserData user) {
		this.user = user;
		this.id = user.getId();
		this.name = user.getName();
		this.token = UUID.randomUUID();
		this.roles = user.getRoleNames();
	}

	public Object getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Serializable getToken() {
		return token;
	}

	public List<String> getRoles() {
		return roles;
	}

	public UserData getUser() {
		return user;
	}

	public boolean hasRole(String... roleNames) {
		for (String roleName : roleNames) {
			if (roles.contains(roleName)) {
				return true;
			}
		}
		return false;
	}

	public static boolean currentHasRole(String... roleNames) {
		Subject currentSubject = getCurrent();
		return currentSubject != null ? currentSubject.hasRole(roleNames) : false;
	}

	public static void setCurrent(Subject subject) {
		Subject.subject.set(subject);
	}

	public static Subject getCurrent() {
		return subject.get();
	}

	public static boolean currentCanAccess(Class<?> clazz) {
		Role role = AnnotationUtil.getAnnotationOfClassOrPackage(clazz, Role.class);
		if (role == null) {
			return true;
		} else {
			return currentHasRole(role.value());
		}
	}

}
