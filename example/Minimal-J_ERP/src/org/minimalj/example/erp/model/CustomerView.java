package org.minimalj.example.erp.model;

import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.util.StringUtils;

public class CustomerView implements View<Customer> {
	
	public static final CustomerView $ = Keys.of(CustomerView.class);
	
	public Object id;

	public String firstname, surname;
	
	public String display() {
		if (!StringUtils.isBlank(firstname)) {
			return firstname + " " + surname;
		} else {
			return surname;
		}
	}
	
}
