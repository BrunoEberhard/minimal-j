package org.minimalj.persistence.sql;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class O {
	public static final O $ = Keys.of(O.class);
	
	public O() {
		// needed for reflection constructor
	}
	
	public O(String oName) {
		this.oName = oName;
	}
	
	public Object id;
	
	@Size(30)
	public String oName;
	
}
