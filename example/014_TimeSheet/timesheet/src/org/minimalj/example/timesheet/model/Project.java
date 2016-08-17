package org.minimalj.example.timesheet.model;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Project {

	public static final Project $ = Keys.of(Project.class);
	
	public Object id;

	@Size(255)
	public String name;

	@Size(1023)
	public String description;

}
