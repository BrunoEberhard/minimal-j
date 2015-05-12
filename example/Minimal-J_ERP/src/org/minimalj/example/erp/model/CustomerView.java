package org.minimalj.example.erp.model;

import java.util.Locale;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.View;
import org.minimalj.util.StringUtils;

public class CustomerView implements View<Customer>, Rendering {
	
	public static final CustomerView $ = Keys.of(CustomerView.class);
	
	public Object id;

	public String firstname, surname;
	
	@Override
	public String render(RenderType renderType, Locale locale) {
		if (!StringUtils.isBlank(firstname)) {
			return firstname + " " + surname;
		} else {
			return surname;
		}
	}
	
}
