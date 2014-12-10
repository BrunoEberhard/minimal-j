package org.minimalj.example.notes;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;

public class Note {
	public static final Note $ = Keys.of(Note.class);
	
	public Object id;
	
	public LocalDate date;
	
	@Size(2000) @Searched
	public String text;
}
