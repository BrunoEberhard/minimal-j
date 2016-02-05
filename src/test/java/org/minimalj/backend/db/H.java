package org.minimalj.backend.db;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class H {

	public static final H $ = Keys.of(H.class);
	
	public H() {
		// needed for reflection constructor
	}

	public Object id;

	@Size(20)
	public String name;
	
	public G g;
	
	public K k;
	
	public final I i = new I();
}
