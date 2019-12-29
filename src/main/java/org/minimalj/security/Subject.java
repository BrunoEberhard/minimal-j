package org.minimalj.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.minimalj.transaction.Role;
import org.minimalj.transaction.TransactionAnnotations;

public class Subject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final InheritableThreadLocal<Subject> subject = new InheritableThreadLocal<>();
	private final String name;
	private final Serializable token;
	private final List<String> roles;

	/**
	 * Only for tests
	 * 
	 * @param name
	 */
	public Subject(String name) {
		this.name = name;
		this.token = null;
		this.roles = Collections.emptyList();
	}

	public Subject(String name, Serializable token, List<String> roles) {
		super();
		this.name = name;
		this.token = token;
		this.roles = roles;
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
		Role role = TransactionAnnotations.getAnnotationOfClassOrPackage(clazz, Role.class);
		if (role == null) {
			return true;
		} else {
			return currentHasRole(role.value());
		}
	}

}
