package org.minimalj.example.library.frontend.form;

import static org.minimalj.example.library.model.Book.*;

import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.edit.form.Form;

public class BookForm extends Form<Book> {

	public BookForm(boolean editable) {
		super(editable, 2);
		
		line($.title);
		line($.author, $.date);
		line($.media, $.pages);
		line($.available, $.price);
	}
	
}
