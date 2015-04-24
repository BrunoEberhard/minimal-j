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
import org.minimalj.frontend.editor.EditorAction;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.frontend.toolkit.Action;

public class MjExampleApplication extends Application {

	public MjExampleApplication() {
	}

	@Override
	public List<Action> getActionsNew() {
		List<Action> items = new ArrayList<>();
		items.add(new EditorAction(new AddBookEditor()));
		items.add(new EditorAction(new AddCustomerEditor()));
		items.add(new EditorAction(new AddLendEditor()));
		return items;
	}

	@Override
	public SearchPage[] getSearchPages() {
		return new SearchPage[]{new BookSearchPage(), new CustomerSearchPage()};
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[]{Book.class, Customer.class, Lend.class};
	}
}
