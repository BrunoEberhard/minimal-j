package ch.openech.mj.example.model;

import java.io.Serializable;

import org.joda.time.LocalDate;

import ch.openech.mj.example.ExampleFormats;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.Search;
import ch.openech.mj.model.annotation.Size;

public class Customer implements Serializable {

	public static final Customer CUSTOMER = Keys.of(Customer.class);

	public static final Search<Customer> BY_FULLTEXT = new Search<>(CUSTOMER.firstName, CUSTOMER.name);

	public int id;

	@Size(ExampleFormats.NAME)
	public String firstName, name;
	public LocalDate birthDay;
	
	@Size(2000)
	public String remarks;
	
	public String display() {
		return firstName + " " + name;
	}
	
}
