package org.minimalj.backend.db;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.ViewReference;

public class I {

	public static final I $ = Keys.of(I.class);
	
	public I() {
		// needed for reflection constructor
	}

	@ViewReference
	public G rG;
}
