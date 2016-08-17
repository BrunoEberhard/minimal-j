package org.minimalj.example.timesheet.model;

import java.math.BigDecimal;

import org.minimalj.model.Keys;

public class WorkForProject {

	public static final WorkForProject $ = Keys.of(WorkForProject.class);

	public Project project;
	
	public BigDecimal hours;
	
}
