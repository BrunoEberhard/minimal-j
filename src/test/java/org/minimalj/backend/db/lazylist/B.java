package org.minimalj.backend.db.lazylist;

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

	public List<C> cList;

	@Size(30)
	public String bName;
}
