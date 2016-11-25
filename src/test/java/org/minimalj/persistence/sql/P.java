package org.minimalj.persistence.sql;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;

public class P {
	public static final P $ = Keys.of(P.class);
	
	public Object id;
	
	@NotEmpty
	public Boolean notEmptyBoolean;
	
	public Boolean optionalBoolean;
}
