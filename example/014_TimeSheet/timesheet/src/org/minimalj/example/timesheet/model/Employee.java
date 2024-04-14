package org.minimalj.example.timesheet.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Employee {

	public static final Employee $ = Keys.of(Employee.class);
	
	public Object id;

	@Size(255)
	public String name;
	
}
