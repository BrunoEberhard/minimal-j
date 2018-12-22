package org.minimalj.repository.sql;

import org.minimalj.model.annotation.Size;

public class F {

	public Object id;
	public int version;
	public boolean historized;

	@Size(20)
	public String f;
}
