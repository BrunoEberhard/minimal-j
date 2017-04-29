package org.minimalj.repository.memory;

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
