package org.minimalj.example.library.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;

public class Customer  {
	public static final Customer $ = Keys.of(Customer.class);

	public Object id;

	@Size(ExampleFormats.NAME) @Searched
	public String firstName, name;
	public LocalDate dateOfBirth;
	
	@Size(2000)
	public String remarks;
	
	@Override
	public String toString() {
		return firstName + " " + name;
	}
}
