package org.minimalj.example.petclinic.model;

import org.minimalj.model.Code;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class PetType implements Code, Rendering {

	public Object id;

	@NotEmpty @Size(80)
	public String name;
	
	@Override
	public String render(RenderType renderType) {
		return name;
	}
	
}
