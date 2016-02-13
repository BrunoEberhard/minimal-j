package org.minimalj.backend.db.lazylist;

import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class A {

	public static final A $ = Keys.of(A.class);

	public A() {
		// needed for reflection constructor
	}

	public A(String aName) {
		this.aName = aName;
	}

	public Object id;

	@Size(30)
	public String aName;

	public List<B> bList;
}
