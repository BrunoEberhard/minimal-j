package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Book.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.page.AbstractSearchPage.SimpleSearchPage;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.transaction.criteria.Criteria;


public class BookSearchPage extends SimpleSearchPage<Book> {

	public static final Object[] FIELDS = {
		$.title, //
		$.author, //
		$.date, //
		$.media, //
		$.pages, //
		$.available, //
	};
	
	public BookSearchPage() {
		super(FIELDS);
	}
	
	@Override
	protected List<Book> load(String query) {
		return Backend.getInstance().read(Book.class, Criteria.search(query), 100);
	}

	@Override
	protected ObjectPage<Book> createPage(Book initialObject) {
		return new BookPage(initialObject);
	}

}
