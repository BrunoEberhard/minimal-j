package org.minimalj.example.helloworld3;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class User {
	public static final User $ = Keys.of(User.class);
	
	@NotEmpty @Size(255)
	public String name;
	
	public byte[] image;
}
