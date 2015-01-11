package org.minimalj.backend.db;

import org.minimalj.model.annotation.Size;

public class C {

	public C() {
		// needed for reflection constructor
	}
	
	public C(String cName) {
		this.cName = cName;
	}
	
	@Size(30)
	public String cName;
	
}
