package org.minimalj.security.model;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.minimalj.model.Code;

public interface UserData extends Code {
	
	public Object getId();
	
	public String getName();
	
	public Password getPassword();
	
	public List<UserRole> getRoles();
	
	public default String format() {
		StringBuilder s = new StringBuilder();
		s.append(getName()).append(" = ");
		s.append(Base64.getEncoder().encodeToString(getPassword().hash)).append(", ");
		s.append(Base64.getEncoder().encodeToString(getPassword().salt));
		for (UserRole role : getRoles()) {
			s.append(", ").append(role.name);
		}
		return s.toString();
	}

	public default List<String> getRoleNames() {
		return getRoles().stream().map(role -> role.name).collect(Collectors.toList());
	}

}