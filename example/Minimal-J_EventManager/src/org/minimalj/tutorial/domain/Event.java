package org.minimalj.tutorial.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.annotation.Size;

public class Event {

	public long id;
	
	public LocalDate date;
	
	@Size(255)
	public String title;
	
	public final List<Participation> participations = new ArrayList<>();
}
