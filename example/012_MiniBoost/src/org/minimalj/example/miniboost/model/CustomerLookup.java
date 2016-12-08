package org.minimalj.example.miniboost.model;

import org.minimalj.model.Keys;
import org.minimalj.model.View;

public class CustomerLookup implements View<Customer> {

	public static final CustomerLookup $ = Keys.of(CustomerLookup.class);

	public Object id;
	
	public String matchcode;

	public String name1, name2, name3;

	public final Address address = new Address();


}