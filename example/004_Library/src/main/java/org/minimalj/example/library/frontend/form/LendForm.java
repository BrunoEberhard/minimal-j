package org.minimalj.example.library.frontend.form;

import static org.minimalj.example.library.model.Lend.*;

import org.minimalj.example.library.model.Book;
import org.minimalj.example.library.model.Customer;
import org.minimalj.example.library.model.Lend;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ReferenceFormElement;

public class LendForm extends Form<Lend> {

	public LendForm(boolean editable) {
		super(editable);
		
		line(new ReferenceFormElement<Book>($.book, Book.$.title, Book.$.author));
		line(new ReferenceFormElement<Customer>($.customer, Customer.$.firstName, Customer.$.name));
				
		line($.till);
	}
	
}
