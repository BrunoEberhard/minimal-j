package org.minimalj.example.miniboost.model;

import org.minimalj.model.annotation.Size;

public class Address {

	@Size(50)
	public String street, city;

	@Size(20)
	public String zip;
	
	@Size(2) // TODO reference Country
	public String country;

}
