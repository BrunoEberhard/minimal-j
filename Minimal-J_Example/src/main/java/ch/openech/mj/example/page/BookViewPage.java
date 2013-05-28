package ch.openech.mj.example.page;

import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.BookForm;
import ch.openech.mj.example.ExamplePersistence;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.page.ObjectViewPage;
import ch.openech.mj.page.PageContext;

public class BookViewPage extends ObjectViewPage<Book> {

	private final Book book;

	public BookViewPage(PageContext context, String bookId) {
		super(context);
		book = lookup(bookId);
	}
	
	private static Book lookup(String bookId) {
		return ExamplePersistence.getInstance().book().read(Integer.valueOf(bookId));
	}

	@Override
	protected Book loadObject() {
		return book;
	}

	@Override
	protected IForm<Book> createForm() {
		return new BookForm(false);
	}
	
}
