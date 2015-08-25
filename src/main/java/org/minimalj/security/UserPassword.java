package org.minimalj.security;

import java.io.Serializable;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Required;
import org.minimalj.model.annotation.Size;

public class UserPassword implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final UserPassword $ = Keys.of(UserPassword.class);

	@Size(255) @Required
	public String user;

	@Size(255)
	public char[] password;

}