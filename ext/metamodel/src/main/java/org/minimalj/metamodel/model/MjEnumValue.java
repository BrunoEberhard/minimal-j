package org.minimalj.metamodel.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class MjEnumValue {
	public static final MjEnumValue $ = Keys.of(MjEnumValue.class);
	
	public Integer ord;

	@Size(1024)
	public String name;
}
