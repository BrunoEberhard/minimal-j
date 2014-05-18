package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Book.*;

import java.util.List;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;
import ch.openech.mj.util.IdUtils;


public class BookTablePage extends TablePage<Book> implements RefreshablePage {

	private final String text;
	
	public static final Object[] FIELDS = {
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
	protected void clicked(Book selectedBook, List<Book> selectedObjects) {
		show(BookPage.class, IdUtils.getIdString(selectedBook));
	}
	
	@Override
	public String getTitle() {
		return "Treffer für " + text;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}

	@Override
	protected List<Book> load(String query) {
		return Backend.getInstance().search(Book.class, (String) query, 100);
	}

}
