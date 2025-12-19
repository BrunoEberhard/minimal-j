package org.minimalj.security;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.security.model.UserData;
import org.minimalj.security.model.UserRole;
import org.minimalj.transaction.Role;

public final class Subject implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final InheritableThreadLocal<Subject> subject = new InheritableThreadLocal<>();
	private final UserData user;
	private final Object id;
	private final String name;
	private final Serializable token;

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
	}

	public Subject(UserData user) {
		this.user = user;
		this.id = user.getId();
		this.name = user.getName();
		this.token = UUID.randomUUID();
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

	public UserData getUser() {
		return user;
	}

	public boolean hasRole(String... roleNames) {
		if (user == null) {
			return false;
		}
		List<UserRole> roles = user.getRoles();
		for (String roleName : roleNames) {
			if (roles.stream().anyMatch(r -> r.name.equals(roleName))) {
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
