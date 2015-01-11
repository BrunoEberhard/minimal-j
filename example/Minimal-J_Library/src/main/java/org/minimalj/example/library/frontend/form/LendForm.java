package org.minimalj.example.library.frontend.form;

import static org.minimalj.example.library.model.Lend.*;

import org.minimalj.example.library.model.Book;
import org.minimalj.example.library.model.Customer;
import org.minimalj.example.library.model.Lend;
import org.minimalj.frontend.edit.fields.ReferenceField;
import org.minimalj.frontend.edit.form.Form;

public class LendForm extends Form<Lend> {

	public LendForm(boolean editable) {
		super(editable);
		
		line(new ReferenceField<Book>($.book, Book.$.title, Book.$.author));
		line(new ReferenceField<Customer>($.customer, Customer.$.firstName, Customer.$.name));
				
		line($.till);
	}
	
}
