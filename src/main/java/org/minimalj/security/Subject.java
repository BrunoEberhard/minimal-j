package org.minimalj.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Subject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final InheritableThreadLocal<Subject> subject = new InheritableThreadLocal<>();
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
	
	public boolean hasRole(String... roleNames) {
		for (String roleName : roleNames) {
			if (roles.contains(roleName)) {
				return true;
			}
		}
		return false;
	}

	public static void setCurrent(Subject subject) {
		Subject.subject.set(subject);
	}
	
	public static Subject getCurrent() {
		return subject.get();
	}

}
