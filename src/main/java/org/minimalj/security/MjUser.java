package org.minimalj.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MjUser implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Serializable authentication;
	
	private List<String> roles = new ArrayList<>();

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
