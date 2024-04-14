package org.minimalj.ubersetzung.model;

import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.annotation.Size;

public class UbersetzungView implements View<Ubersetzung> {

	public static final UbersetzungView $ = Keys.of(UbersetzungView.class);
	
	public Object id;
	
	@Size(2)
	public String lang, country;
	
}
