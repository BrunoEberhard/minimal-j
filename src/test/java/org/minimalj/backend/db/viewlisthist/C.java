package org.minimalj.backend.db.viewlisthist;

import org.minimalj.model.annotation.Size;


public class C {

	public C() {
		// needed for reflection constructor
	}
	
	public C(String cName) {
		this.cName = cName;
	}
	
	public Object id;
	
	@Size(30)
	public String cName;
}
