package org.minimalj.application;

import java.io.Serializable;
import java.util.List;

public class Subject {

	private static final ThreadLocal<Subject> subjectByThread = new ThreadLocal<>();
	
	private String user;
	private Serializable authentication;
	
	private List<String> roles;

	public static Subject get() {
		return subjectByThread.get();
	}
	
	public static void set(Subject subject) {
		subjectByThread.set(subject);
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Serializable getAuthentication() {
		return authentication;
	}
	
	public void setAuthentication(Serializable authentication) {
		this.authentication = authentication;
	}
	
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
	public List<String> getRoles() {
		return roles;
	}

}
