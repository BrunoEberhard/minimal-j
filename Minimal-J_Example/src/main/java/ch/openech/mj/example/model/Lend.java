package ch.openech.mj.example.model;

import org.joda.time.LocalDate;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.annotation.Size;

public class Lend {

	public static final Lend LEND = Keys.of(Lend.class);
	
	@Size(36)
	public String book;
	@Size(36)
	public String customer;
	public LocalDate till;
	
}
