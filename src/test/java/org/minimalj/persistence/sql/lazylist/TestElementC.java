package org.minimalj.persistence.sql.lazylist;

import org.minimalj.model.annotation.Size;


public class TestElementC {

	public TestElementC() {
		// needed for reflection constructor
	}
	
	public TestElementC(String name) {
		this.name = name;
	}
	
	public Object id;
	
	@Size(30)
	public String name;
}
