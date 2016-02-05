package org.minimalj.example.library.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;

public class Lend {

	public static final Lend $ = Keys.of(Lend.class);
	
	public Object id;
	
	public Book book;

	public Customer customer;
	
	public LocalDate till;
	
}
