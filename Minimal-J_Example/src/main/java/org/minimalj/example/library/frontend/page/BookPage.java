package org.minimalj.example.library.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.frontend.form.BookForm;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectViewPage;
import org.minimalj.frontend.page.PageContext;

public class BookPage extends ObjectViewPage<Book> {

	private final Book book;

	public BookPage(PageContext context, String id) {
		super(context);
		book = lookup(id);
	}
	
	private static Book lookup(String key) {
		return Backend.getInstance().read(Book.class, Long.valueOf(key));
	}

	@Override
	protected Form<Book> createForm() {
		return new BookForm(false);
	}

	@Override
	public String getTitle() {
		return "Buch " + book.title;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}

	@Override
	protected Book getObject() {
		return book;
	}
	
}
