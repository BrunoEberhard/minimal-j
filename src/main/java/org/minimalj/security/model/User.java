package org.minimalj.security.model;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.security.Password;

public class User {
	public static final User $ = Keys.of(User.class);
	
	public Object id;
	
	@Size(255)
	public String name;
	
	public final Password password = new Password();
	
	public List<UserRole> roles = new ArrayList<>();
	
	public String format() {
		StringBuilder s = new StringBuilder();
		s.append(name).append(" = ");
		s.append(Base64.getEncoder().encodeToString(password.hash)).append(", ");
		s.append(Base64.getEncoder().encodeToString(password.salt));
		for (UserRole role : roles) {
			s.append(", ").append(role.name);
		}
		return s.toString();
	}
}