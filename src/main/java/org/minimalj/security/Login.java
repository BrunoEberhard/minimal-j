package org.minimalj.security;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Required;
import org.minimalj.model.annotation.Size;

public class Login {
	public static final Login $ = Keys.of(Login.class);

	@Size(255) @Required
	public String user;

	@Size(255)
	public Object password;

}