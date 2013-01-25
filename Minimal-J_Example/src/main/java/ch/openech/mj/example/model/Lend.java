package ch.openech.mj.example.model;

import org.joda.time.LocalDate;

import ch.openech.mj.model.Keys;

public class Lend {

	public static final Lend LEND = Keys.of(Lend.class);
	
	public Book book;
	public Customer customer;
	public LocalDate till;
	
}
