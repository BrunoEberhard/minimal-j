package org.minimalj.example.library;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.library.frontend.editor.AddBookEditor;
import org.minimalj.example.library.frontend.editor.AddCustomerEditor;
import org.minimalj.example.library.frontend.editor.AddLendEditor;
import org.minimalj.example.library.frontend.page.BookSearchPage;
import org.minimalj.example.library.frontend.page.CustomerSearchPage;
import org.minimalj.example.library.model.Book;
import org.minimalj.example.library.model.Customer;
import org.minimalj.example.library.model.Lend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.SearchPage;

public class MjExampleApplication extends Application {

	public MjExampleApplication() {
	}

	@Override
	public List<Action> getNavigation() {
		List<Action> items = new ArrayList<>();
		items.add(new AddBookEditor());
		items.add(new AddCustomerEditor());
		items.add(new AddLendEditor());
		return items;
	}

	@Override
	public void search(String query) {
		BookSearchPage bookSearchPage = new BookSearchPage(query);
		CustomerSearchPage customerSearchPage = new CustomerSearchPage(query);
		SearchPage.handle(bookSearchPage, customerSearchPage);
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[]{Book.class, Customer.class, Lend.class};
	}
}
