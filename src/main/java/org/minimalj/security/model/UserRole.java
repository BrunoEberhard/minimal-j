package org.minimalj.security.model;

import org.minimalj.model.annotation.Size;

public class UserRole {
	
	public UserRole() {
		//
	}
	
	public UserRole(String roleName) {
		this.name = roleName;
	}
	
	@Size(255)
	public String name;
}