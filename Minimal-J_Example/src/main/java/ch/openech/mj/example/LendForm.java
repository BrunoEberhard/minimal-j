package ch.openech.mj.example;

import static ch.openech.mj.example.model.Lend.*;
import ch.openech.mj.edit.fields.ReferenceField;
import ch.openech.mj.edit.form.Form;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.Lend;

public class LendForm extends Form<Lend> {

	public LendForm(boolean editable) {
		super(editable);
		
		line(new ReferenceField<Book>(LEND.book, Book.BY_FULLTEXT, new Object[] { Book.BOOK.title, Book.BOOK.author }));
		line(new ReferenceField<Customer>(LEND.customer, Customer.BY_FULLTEXT, new Object[] { Customer.CUSTOMER.firstName, Customer.CUSTOMER.name }));
				
		line(LEND.till);
	}
	
}
