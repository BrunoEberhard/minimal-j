package org.minimalj.example.library.model;

import org.threeten.bp.LocalDate;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.View;

public class Lend {

	public static final Lend LEND = Keys.of(Lend.class);
	
	public Object id;
	
	@View
	public Book book;

	@View
	public Customer customer;
	
	public LocalDate till;
	
}
