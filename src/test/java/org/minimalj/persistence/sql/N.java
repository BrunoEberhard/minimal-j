package org.minimalj.persistence.sql;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class N {
	public static final N $ = Keys.of(N.class);
	
	public N() {
		// needed for reflection constructor
	}

	public N(String n) {
		this.n = n;
	}

	public Object id;

	@Size(20)
	public String n;
	
	public final List<O> oList = new ArrayList<>();

	public O oReference;
}
