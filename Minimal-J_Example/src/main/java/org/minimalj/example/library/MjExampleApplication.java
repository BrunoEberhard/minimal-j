package org.minimalj.example.library;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.MjApplication;
import org.minimalj.example.library.frontend.editor.AddBookEditor;
import org.minimalj.example.library.frontend.editor.AddCustomerEditor;
import org.minimalj.example.library.frontend.editor.AddLendEditor;
import org.minimalj.example.library.frontend.page.BookTablePage;
import org.minimalj.example.library.frontend.page.CustomerTablePage;
import org.minimalj.example.library.model.Book;
import org.minimalj.example.library.model.Customer;
import org.minimalj.example.library.model.Lend;
import org.minimalj.frontend.page.EditorPageAction;
import org.minimalj.frontend.page.PageContext;
import org.minimalj.frontend.toolkit.IAction;

public class MjExampleApplication extends MjApplication {

	public MjExampleApplication() {
	}

	@Override
	public List<IAction> getActionsNew(PageContext context) {
		List<IAction> items = new ArrayList<>();
		items.add(new EditorPageAction(new AddBookEditor()));
		items.add(new EditorPageAction(new AddCustomerEditor()));
		items.add(new EditorPageAction(new AddLendEditor()));
		return items;
	}

	@Override
	public String getWindowTitle(PageContext pageContext) {
		return "Minimal-J Example Application";
	}

	@Override
	public Class<?>[] getSearchClasses() {
		return new Class<?>[]{BookTablePage.class, CustomerTablePage.class};
	}

	@Override
	public Class<?> getPreferencesClass() {
		return null;
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[]{Book.class, Customer.class, Lend.class};
	}
}
