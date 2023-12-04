package org.minimalj.security.model;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class User implements UserData {
	public static final User $ = Keys.of(User.class);
	
	public Object id;
	
	@Size(255)
	public String name;
	
	public final Password password = new Password();
	
	public List<UserRole> roles = new ArrayList<>();

	@Override
	public Object getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Password getPassword() {
		return password;
	}
	
	@Override
	public List<UserRole> getRoles() {
		return roles;
	}
}