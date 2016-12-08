package org.minimalj.example.miniboost.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class ProjectCost {

	public static final ProjectCost $ = Keys.of(ProjectCost.class);

	public Object id;
	
	@NotEmpty
	public Project project;

	@NotEmpty
	public Employee employee;
	
	public LocalDate checkIn, checkOut;

	@Size(12)
	public BigDecimal pause, hours, hourCostRate, totalCost, hourTurnoverRate, hourTurnover;
	
	@Size(255)
	public String text;
	
	@Size(50)
	public String workAs;

}