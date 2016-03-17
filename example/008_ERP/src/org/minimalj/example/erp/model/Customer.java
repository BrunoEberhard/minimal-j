package org.minimalj.example.erp.model;

import java.time.LocalDate;

import org.fluttercode.datafactory.impl.DataFactory;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.mock.Mocking;

public class Customer implements Mocking {	
	public static final Customer $ = Keys.of(Customer.class);
	
	public Object id;
	
	@Size(32) @Searched
	public String customerNr;

	@Size(50) @Searched
	public String firstname, surname;
	
	@Size(50)
	public String company;

	public Title title;
	public Salutation salutation;
	
	@Size(50)
	public String email;

	@Size(50)
	public String address, zip, city;

	public LocalDate customersince;

	@Override
	public void mock() {
		customerNr = "CN - " + (int)(Math.random() * 9000 + 1000);
		DataFactory df = new DataFactory();
		surname = df.getLastName();
		firstname = df.getFirstName();
		boolean male = Math.random() < 0.5;
		email = firstname.toLowerCase() + "." + surname.toLowerCase() + "@loremipsum.com";
		company = surname + " " + (Math.random() < 0.5 ? "AG" : "GmbH");
		salutation = male ? Salutation.Male : Salutation.Female;
		customersince = LocalDate.now();
	}
}
