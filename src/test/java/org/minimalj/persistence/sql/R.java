package org.minimalj.persistence.sql;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class R {
	public static final R $ = Keys.of(R.class);
	
	public Object id;
	public int version;
	public boolean historized;

	@Size(255)
	public String string;
}
