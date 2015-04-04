package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Book.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.page.AbstractSearchPage;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.transaction.criteria.Criteria;


public class BookSearchPage extends AbstractSearchPage<Book> {

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
	public void action(Book selectedBook, List<Book> selectedObjects) {
		ClientToolkit.getToolkit().show(new BookPage(selectedBook));
	}
	
	@Override
	protected List<Book> load(String query) {
		return Backend.getInstance().read(Book.class, Criteria.search(query), 100);
	}

}
