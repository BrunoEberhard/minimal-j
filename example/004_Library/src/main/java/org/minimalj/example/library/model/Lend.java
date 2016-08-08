package org.minimalj.example.library.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;

public class Lend {

	public static final Lend $ = Keys.of(Lend.class);
	
	public Object id;
	
	@NotEmpty
	public Book book;

	@NotEmpty
	public Customer customer;
	
	@NotEmpty
	public LocalDate till;
	
}
