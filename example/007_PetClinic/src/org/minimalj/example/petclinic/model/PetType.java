package org.minimalj.example.petclinic.model;

import org.minimalj.model.Code;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.LocaleContext;

public class PetType implements Code, Rendering {

	public Object id;

	@NotEmpty @Size(80)
	public String name, nameDe;
	
	@Override
	public String render() {
		if (LocaleContext.getCurrent().getLanguage().startsWith("de")) {
			return nameDe;
		} else {
			return name;
		}
	}
	
}
