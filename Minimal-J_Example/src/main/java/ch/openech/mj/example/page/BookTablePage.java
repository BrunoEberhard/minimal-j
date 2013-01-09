package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Book.BOOK;

import java.util.List;

import ch.openech.mj.example.ExamplePersistence;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;


public class BookTablePage extends TablePage<Book> implements RefreshablePage {

	private static final Object[] FIELDS = {
		BOOK.title, //
		BOOK.author, //
		BOOK.date, //
		BOOK.media, //
		BOOK.pages, //
		BOOK.available, //
	};

	public BookTablePage(PageContext context, String text) {
		super(context, FIELDS, text);
	}
	
	@Override
	protected void clicked(Book book) {
		int id = ExamplePersistence.getInstance().book().getId(book);
		show(BookViewPage.class, String.valueOf(id));
	}

	@Override
	protected List<Book> find(String text) {
		return ExamplePersistence.getInstance().book().find(text);
	}
	
}
