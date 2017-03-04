package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Book.$;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.repository.query.By;


public class BookSearchPage extends SearchPage<Book> {

	public static final Object[] FIELDS = {
		$.title, //
		$.author, //
		$.date, //
		$.media, //
		$.pages, //
		$.available, //
	};
	
	public BookSearchPage(String query) {
		super(query, FIELDS);
	}
	
	@Override
	protected List<Book> load(String query, Object[] sortKey, boolean[] sortDirection, int offset, int rows) {
		return Backend.find(Book.class, By.search(query).limit(offset, rows));
	}

	@Override
	public ObjectPage<Book> createDetailPage(Book initialObject) {
		return new BookPage(initialObject);
	}

}
