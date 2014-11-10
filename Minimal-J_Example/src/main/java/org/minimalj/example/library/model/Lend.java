package org.minimalj.example.library.model;

import java.time.LocalDate;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Reference;

public class Lend {

	public static final Lend LEND = Keys.of(Lend.class);
	
	public Object id;
	
	@Reference
	public Book book;

	@Reference
	public Customer customer;
	
	public LocalDate till;
	
}
