package org.minimalj.example.library.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.threeten.bp.LocalDate;

public class Customer  {
	public static final Customer CUSTOMER = Keys.of(Customer.class);

	public int id;

	@Size(ExampleFormats.NAME) @Searched
	public String firstName, name;
	public LocalDate birthDay;
	
	@Size(2000)
	public String remarks;
	
	@Override
	public String toString() {
		return firstName + " " + name;
	}
}
