package org.minimalj.example.miniboost.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.fluttercode.datafactory.impl.DataFactory;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.mock.Mocking;

public class Project implements Mocking {

	public static final Project $ = Keys.of(Project.class);

	public Object id;
	
	public Customer customer;
	
	@Size(25) @NotEmpty @Searched
	public String matchcode;

	@Size(100) @NotEmpty @Searched
	public String name1;

	public final Address address = new Address();

	public Employee crewChief;
	
	@Size(500)
	public String description;
	
	public LocalDate startDate, endDate, closeDate;
	
	@Size(12)
	public BigDecimal cost, amount;
	
	@Override
    public void mock() {
		DataFactory df = new DataFactory();
		name1 = df.getName();
		matchcode = name1;
		address.city = df.getCity();
		address.street = df.getStreetName();
    }

}