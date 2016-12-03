package org.minimalj.persistence.sql;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Q {
	public static final Q $ = Keys.of(Q.class);
	
	public Object id;
	public int version;

	@Size(255)
	public String string;
}
