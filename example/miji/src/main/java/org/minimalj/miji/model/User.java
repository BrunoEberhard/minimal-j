package org.minimalj.miji.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class User {

	public static final User $ = Keys.of(User.class);
	
	public Object id;
	
	@Size(255)
	public String name, displayName, key, accountId, emailAddress;
	
}
