package org.minimalj.backend.db.viewlisthist;

import java.util.List;

import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.ViewReference;


public class B {

	public B() {
		// needed for reflection constructor
	}
	
	public B(String bName) {
		this.bName = bName;
	}
	
	public Object id;
	public int version;
	
	@ViewReference
	public List<C> c;
	
	@Size(30)
	public String bName;
}
