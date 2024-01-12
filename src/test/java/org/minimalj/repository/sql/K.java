package org.minimalj.repository.sql;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class K {
	public static final K $ = Keys.of(K.class);

	public K() {
		// needed for reflection constructor
	}

	public K(String k) {
		this.k = k;
	}

	@Size(20)
	public String k;
}
