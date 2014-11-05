package org.minimalj.backend.db;

import org.minimalj.model.annotation.Size;

public class K {

	public K() {
		// needed for reflection constructor
	}

	public K(String k) {
		this.k = k;
	}

	@Size(20)
	public String k;
}
