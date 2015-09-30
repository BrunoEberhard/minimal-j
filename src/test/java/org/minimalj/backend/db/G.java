package org.minimalj.backend.db;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class G {
	public static final G $ = Keys.of(G.class);
	
	public G() {
		// needed for reflection constructor
	}

	public G(String g) {
		this.g = g;
	}

	public Object id;

	@Size(20)
	public String g;
}
