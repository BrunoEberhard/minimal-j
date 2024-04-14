package org.minimalj.example.timesheet.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;

public class Work {

	public static final Work $ = Keys.of(Work.class);
	
	public Object id;
	
	public LocalDate date;

	public List<WorkForProject> workForProjects = new ArrayList<>();
	
}
