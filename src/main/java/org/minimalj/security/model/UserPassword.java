package org.minimalj.security.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class UserPassword {
	public static final UserPassword $ = Keys.of(UserPassword.class);

	@Size(255) @NotEmpty
	public String user;

	@Size(255)
	public char[] password;

}