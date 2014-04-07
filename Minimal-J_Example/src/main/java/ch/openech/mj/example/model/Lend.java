package ch.openech.mj.example.model;

import org.joda.time.LocalDate;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.annotation.View;

public class Lend {

	public static final Lend LEND = Keys.of(Lend.class);
	
	public int id;
	
	@View
	public Book book;

	@View
	public Customer customer;
	
	public LocalDate till;
	
}
