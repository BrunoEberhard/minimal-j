package org.minimalj.backend.db;

import org.minimalj.model.annotation.Size;

public class G {

	public G() {
		// needed for reflection constructor
	}

	public G(String g) {
		this.g = g;
	}

	public int id;

	@Size(20)
	public String g;
}
