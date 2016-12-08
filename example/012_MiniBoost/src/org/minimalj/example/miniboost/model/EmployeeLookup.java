package org.minimalj.example.miniboost.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.View;

public class EmployeeLookup implements View<Employee> {

	public static final EmployeeLookup $ = Keys.of(EmployeeLookup.class);

	public Object id;
	
	public String matchcode;

	public LocalDate startDate;
	public LocalDate endDate;
	
	public String firstname, lastname;

	public final Address address = new Address();

}
