package org.minimalj.example.erp.model;

import java.time.LocalDate;

import org.minimalj.backend.Backend;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.mock.MockPrename;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.mock.MockName;

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
		customerNr = "CN - " + (Backend.getInstance().executeStatement(Integer.class, "MaxCustomer") + 1);
		boolean male = Math.random() < 0.5;
		firstname = MockPrename.getFirstName(male);
		surname = MockName.officialName();
		salutation = male ? Salutation.Male : Salutation.Female;
		customersince = LocalDate.now();
	}
}
