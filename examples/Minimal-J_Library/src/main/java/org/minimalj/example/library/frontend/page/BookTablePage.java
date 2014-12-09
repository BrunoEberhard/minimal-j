package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Book.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.IdUtils;


public class BookTablePage extends TablePage<Book> {

	private final String text;
	
	public static final Object[] FIELDS = {
		$.title, //
		$.author, //
		$.date, //
		$.media, //
		$.pages, //
		$.available, //
	};
	
	public BookTablePage(String text) {
		super(FIELDS, text);
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
	protected List<Book> load(String searchText) {
		return Backend.getInstance().read(Book.class, Criteria.search(searchText), 100);
	}

}
