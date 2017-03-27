package org.minimalj.example.miniboost.model;

import java.util.Locale;

import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.LocaleContext;

public class Country implements Code, Rendering {
	public static final Country $ = Keys.of(Country.class);
	
	@Size(2)
	public String id;
	
	@Size(3)
	public String code3, codenum;
	
	@Size(25)
	public String areaCode;
	
	@Size(80)
	public String nameEn, nameDe;
	
	@Override
	public String render(RenderType renderType) {
		return getName();
	}
	
	public String getName() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "name");
		
		if (LocaleContext.getCurrent().equals(Locale.ENGLISH)) {
			return nameEn;
		} else {
			return nameDe;
		}
	}
	
}