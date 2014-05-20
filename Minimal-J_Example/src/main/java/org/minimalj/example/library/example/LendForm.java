package org.minimalj.example.library.example;

import static org.minimalj.example.library.model.Lend.*;

import org.minimalj.example.library.model.Book;
import org.minimalj.example.library.model.Customer;
import org.minimalj.example.library.model.Lend;
import org.minimalj.frontend.edit.fields.ReferenceField;
import org.minimalj.frontend.edit.form.Form;

public class LendForm extends Form<Lend> {

	public LendForm(boolean editable) {
		super(editable);
		
		line(new ReferenceField<Book>(LEND.book, Book.BOOK.title, Book.BOOK.author));
		line(new ReferenceField<Customer>(LEND.customer, Customer.CUSTOMER.firstName, Customer.CUSTOMER.name));
				
		line(LEND.till);
	}
	
}
