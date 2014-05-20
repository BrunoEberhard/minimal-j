package org.minimalj.example.library.example;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Book;
import org.minimalj.example.library.page.BookPage;
import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.form.IForm;
import org.minimalj.frontend.page.PageLink;

public class AddBookEditor extends Editor<Book> {

	@Override
	public IForm<Book> createForm() {
		return new BookForm(true);
	}
	
	@Override
	public String save(Book book) throws Exception {
		long id = Backend.getInstance().insert(book);
		return PageLink.link(BookPage.class, id);
	}

	@Override
	public String getTitle() {
		return "Buch hinzufügen";
	}

}
