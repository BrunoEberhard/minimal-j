package org.minimalj.ubersetzung.model;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Ubersetzung {

	public static final Ubersetzung $ = Keys.of(Ubersetzung.class);
	
	public Object id;
	
	@Size(2)
	public String lang, country;

	public final List<UbersetzungEntry> entries = new ArrayList<>();
}
