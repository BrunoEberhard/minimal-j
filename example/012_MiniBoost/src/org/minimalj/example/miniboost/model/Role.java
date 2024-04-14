package org.minimalj.example.miniboost.model;

import org.minimalj.model.Code;
import org.minimalj.model.annotation.Size;

public class Role implements Code {

	public static final Role ADMIN = new Role("ADMIN", true);
	public static final Role USER = new Role("USER", false);
	
	@Size(15)
	public String id;
	public Boolean admin;

	public Role() {
	}
	
	public Role(String id, boolean admin) {
		this.id = id;
		this.admin = admin;
	}

}