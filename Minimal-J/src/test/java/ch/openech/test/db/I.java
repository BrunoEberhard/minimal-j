package ch.openech.test.db;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.annotation.View;

public class I {

	public static final I I_ = Keys.of(I.class);
	
	public I() {
		// needed for reflection constructor
	}

	@View
	public G rG;
}
