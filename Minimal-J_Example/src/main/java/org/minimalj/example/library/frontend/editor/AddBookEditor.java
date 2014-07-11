package org.minimalj.example.library.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.frontend.form.BookForm;
import org.minimalj.example.library.frontend.page.BookPage;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.PageLink;

public class AddBookEditor extends Editor<Book> {

	@Override
	public Form<Book> createForm() {
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
