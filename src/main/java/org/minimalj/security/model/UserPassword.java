package org.minimalj.security.model;

import java.io.Serializable;

import org.minimalj.application.Configuration;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Autocomplete;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class UserPassword implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final UserPassword $ = Keys.of(UserPassword.class);

	@Size(255) @NotEmpty @Autocomplete(Autocomplete.USERNAME)
	public String user;

	@Size(255) @Autocomplete(Autocomplete.CURRENT_PASSWORD)
	public char[] password;

	public transient Boolean rememberMe = Configuration.isDevModeActive();

}