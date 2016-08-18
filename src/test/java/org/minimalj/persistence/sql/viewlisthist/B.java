package org.minimalj.persistence.sql.viewlisthist;

import java.util.List;

import org.minimalj.model.annotation.Size;


public class B {

	public B() {
		// needed for reflection constructor
	}
	
	public B(String bName) {
		this.bName = bName;
	}
	
	public Object id;
	public int version;
	
	public List<C> c;
	
	@Size(30)
	public String bName;
}
