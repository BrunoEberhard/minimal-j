package org.minimalj.ubersetzung.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class UbersetzungEntry {

	public static final UbersetzungEntry $ = Keys.of(UbersetzungEntry.class);
	
	@Size(255) @NotEmpty
	public String key;
	
	@Size(255)
	public String value;
}
