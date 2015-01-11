package org.minimalj.example.library.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.frontend.form.BookForm;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.ObjectPage;

public class BookPage extends ObjectPage<Book> {

	private final String idString;

	public BookPage(String idString) {
		this.idString = idString;
	}
	
	@Override
	public Book loadObject() {
		return Backend.getInstance().read(Book.class, idString);
	}

	@Override
	protected Form<Book> createForm() {
		return new BookForm(false);
	}

	@Override
	public String getTitle() {
		return "Buch " + getObject().title;
	}
}
