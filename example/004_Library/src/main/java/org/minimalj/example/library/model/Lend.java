package org.minimalj.example.library.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.ViewReference;

public class Lend {

	public static final Lend $ = Keys.of(Lend.class);
	
	public Object id;
	
	@ViewReference
	public Book book;

	@ViewReference
	public Customer customer;
	
	public LocalDate till;
	
}
