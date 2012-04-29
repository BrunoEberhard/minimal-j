package ch.openech.mj.example;

import java.sql.SQLException;

import ch.openech.mj.application.ObjectViewPage;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.page.PageContext;

public class BookViewPage extends ObjectViewPage<Book> {

	private final Book book;

	public BookViewPage(PageContext context, String bookId) {
		super(context);
		book = lookup(bookId);
	}
	
	private static Book lookup(String bookId) {
		try {
			return ExamplePersistence.getInstance().book().read(Integer.valueOf(bookId));
		} catch (SQLException x) {
			throw new RuntimeException("Konnte Buch nicht laden", x);
		}
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
