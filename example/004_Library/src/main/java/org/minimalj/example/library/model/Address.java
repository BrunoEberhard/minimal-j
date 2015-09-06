package org.minimalj.example.library.model;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.Size;

public class Address implements Rendering {
	public static final Address $ = Keys.of(Address.class);

	@Size(ExampleFormats.NAME)
	public String street, city;
	
	@Override
	public String render(RenderType renderType) {
		return street + " " + city;
	}
}
