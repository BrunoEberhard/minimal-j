package ch.openech.mj.example.model;

import java.io.Serializable;

import org.joda.time.LocalDate;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.Reference;

public class Lend implements Serializable {

	public static final Lend LEND = Keys.of(Lend.class);
	
	public long id;
	
	public final Reference<Book> book = new Reference<>(Book.BOOK.title, Book.BOOK.author);
 	public final Reference<Customer> customer = new Reference<>(Customer.CUSTOMER.firstName, Customer.CUSTOMER.name);
	public LocalDate till;
	
}
