package ch.openech.mj.example.model;

import org.joda.time.LocalDate;

import ch.openech.mj.example.ExampleFormats;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.annotation.Size;

public class Customer {

	public static final Customer CUSTOMER = Keys.of(Customer.class);
	
	@Size(ExampleFormats.NAME)
	public String firstName, name;
	public LocalDate birthDay;
	
}
