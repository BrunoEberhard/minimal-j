package org.minimalj.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;

public class Subject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final ThreadLocal<Subject> subject = new ThreadLocal<>();
	private String name;
	private Serializable token;
	
	private final List<String> roles = new ArrayList<>();

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Serializable getToken() {
		return token;
	}
	
	public void setToken(Serializable token) {
		this.token = token;
	}
	
	public List<String> getRoles() {
		return roles;
	}
	
	public boolean isValid() {
		return token != null;
	}

	public static boolean hasRoleFor(Transaction<?> transaction) {
		Role role = getRole(transaction);
		boolean noRoleNeeded = role == null;
		return noRoleNeeded || hasRole(role.value());
	}
	
	public static boolean hasRole(String... roleNames) {
		Subject subject = getSubject();
		if (subject != null) {
			for (String roleName : roleNames) {
				if (subject.roles.contains(roleName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static Role getRole(Transaction<?> transaction) {
		Role role = transaction.getClass().getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		role = transaction.getClass().getPackage().getAnnotation(Role.class);
		return role;
	}
	
	public static void setSubject(Subject subject) {
		Subject.subject.set(subject);
	}
	
	public static Subject getSubject() {
		return subject.get();
	}

}
