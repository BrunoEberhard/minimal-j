package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Book.*;

import java.util.List;

import ch.openech.mj.example.ExamplePersistence;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;


public class BookTablePage extends TablePage<Book> implements RefreshablePage {

	private final String text;
	
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
		this.text = text;
	}
	
	@Override
	protected void clicked(Book book) {
		int id = ExamplePersistence.getInstance().book().getId(book);
		show(BookPage.class, String.valueOf(id));
	}

	@Override
	protected List<Book> find(String text) {
		return ExamplePersistence.getInstance().bookIndex().find(text);
	}

	@Override
	public String getTitle() {
		return "Treffer für " + text;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}
	
}
