package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Lend.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Customer;
import org.minimalj.example.library.model.Lend;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.PageContext;
import org.minimalj.frontend.page.RefreshablePage;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;


public class LendTablePage extends TablePage<Lend> implements RefreshablePage {

	private final String text;
	
	public static final Object[] FIELDS = {
		LEND.book.title, //
		LEND.book.author, //
		LEND.till
	};
	
	public LendTablePage(PageContext context, String text) {
		super(context, FIELDS, text);
		this.text = text;
	}

	@Override
	public String getTitle() {
		return "Ausleihen";
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}

	@Override
	protected List<Lend> load(String query) {
		Customer customer = Backend.getInstance().read(Customer.class, Long.valueOf(query));
		return Backend.getInstance().read(Lend.class, Criteria.equals(Lend.LEND.customer, customer), 100);
	}

	@Override
	protected void clicked(Lend selectedObject, List<Lend> selectedObjects) {
		// TODO
		// show(BookPage.class, selectedObject.book.);
	}
	
}
