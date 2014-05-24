package org.minimalj.example.library.model;

import java.io.Serializable;

import org.joda.time.LocalDate;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;

public class Customer implements Serializable {

	public static final Customer CUSTOMER = Keys.of(Customer.class);

	public int id;

	@Size(ExampleFormats.NAME) @Searched
	public String firstName, name;
	public LocalDate birthDay;
	
	@Size(2000)
	public String remarks;
	
	public String display() {
		return firstName + " " + name;
	}

	@Override
	public String toString() {
		return firstName + " " + name;
	}
	
	
	
}
