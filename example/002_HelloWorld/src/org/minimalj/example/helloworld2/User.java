package org.minimalj.example.helloworld2;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Required;
import org.minimalj.model.annotation.Size;

public class User {
	public static final User $ = Keys.of(User.class);
	
	@Required @Size(255)
	public String name;
}
