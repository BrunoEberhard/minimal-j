package ch.openech.test.db;

import ch.openech.mj.model.annotation.Size;

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
