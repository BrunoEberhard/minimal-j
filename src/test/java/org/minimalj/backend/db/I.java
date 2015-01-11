package org.minimalj.backend.db;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Reference;

public class I {

	public static final I $ = Keys.of(I.class);
	
	public I() {
		// needed for reflection constructor
	}

	@Reference
	public G rG;
}
