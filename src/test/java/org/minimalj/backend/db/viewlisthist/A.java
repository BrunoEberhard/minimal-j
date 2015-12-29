package org.minimalj.backend.db.viewlisthist;

import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.ViewReference;

public class A {

	public static final A $ = Keys.of(A.class);
	
	public A() {
		// needed for reflection constructor
	}
	
	public A(String aName) {
		this.aName = aName;
	}
	
	public Object id;
	public int version;
	
	@Size(30)
	public String aName;

	@ViewReference
	public List<B> b;

}
