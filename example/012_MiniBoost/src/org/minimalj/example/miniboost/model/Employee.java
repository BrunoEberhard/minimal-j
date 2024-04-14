package org.minimalj.example.miniboost.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Decimal;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class Employee {

	public static final Employee $ = Keys.of(Employee.class);

	public Object id;
	
	@Size(25) @NotEmpty
	public String matchcode;

	public LocalDate startDate;
	public LocalDate endDate;
	
	@Size(50) @NotEmpty
	public String firstname;
	
	@Size(50)
	public String lastname;

	@Size(50)
	public String firmname1, firmname2, firmname3;

	public final Address address = new Address();

	public final Contact contact = new Contact();
	
	@Size(12) @Decimal(2) @NotEmpty
	public BigDecimal hourRate;

	@Size(50)
	public String profession, workAs, info;
}
