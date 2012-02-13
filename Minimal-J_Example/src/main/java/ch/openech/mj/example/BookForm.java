package ch.openech.mj.example;

import static ch.openech.mj.example.model.Book.BOOK;
import ch.openech.mj.edit.form.AbstractFormVisual;
import ch.openech.mj.example.model.Book;

public class BookForm extends AbstractFormVisual<Book> {

	public BookForm(boolean editable) {
		super(editable, 2);
		
		line(BOOK.title);
		line(BOOK.author, BOOK.date);
		line(BOOK.media, BOOK.pages);
		line(BOOK.available);
	}
	
}
