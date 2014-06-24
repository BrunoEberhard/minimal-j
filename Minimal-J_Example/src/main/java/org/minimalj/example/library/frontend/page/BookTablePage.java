package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Book.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.PageContext;
import org.minimalj.frontend.page.RefreshablePage;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.IdUtils;


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
		return "Search results: " + text;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}

	@Override
	protected List<Book> load(String searchText) {
		return Backend.getInstance().read(Book.class, Criteria.search(searchText), 100);
	}

}
