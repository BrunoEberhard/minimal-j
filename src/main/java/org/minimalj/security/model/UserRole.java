package org.minimalj.security.model;

import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		return Objects.equals(name, ((UserRole) object).name);
	}

}