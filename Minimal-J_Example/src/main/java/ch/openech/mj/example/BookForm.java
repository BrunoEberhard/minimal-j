package ch.openech.mj.example;

import static ch.openech.mj.example.model.Book.*;
import ch.openech.mj.edit.form.Form;
import ch.openech.mj.example.model.Book;

public class BookForm extends Form<Book> {

	public BookForm(boolean editable) {
		super(editable, 2);
		
		line(BOOK.bookIdentification.title);
		line(BOOK.bookIdentification.author, BOOK.date);
		line(BOOK.media, BOOK.pages);
		line(BOOK.available, BOOK.price);
	}
	
}
