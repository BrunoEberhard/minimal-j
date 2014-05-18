package ch.openech.mj.example.page;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.BookForm;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.ObjectViewPage;
import ch.openech.mj.page.PageContext;

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
	protected IForm<Book> createForm() {
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
