package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Customer.*;

import java.util.List;

import ch.openech.mj.example.MjExampleApplication;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;
import ch.openech.mj.search.IndexSearch;


public class CustomerTablePage extends TablePage<Customer> implements RefreshablePage {

	private final String text;
	
	public static final Object[] FIELDS = {
		CUSTOMER.customerIdentification.firstName, //
		CUSTOMER.customerIdentification.name, //
		CUSTOMER.customerIdentification.birthDay, //
	};
	
	public CustomerTablePage(PageContext context, String text) {
		super(context, new IndexSearch<>(MjExampleApplication.persistence().customerIndex), FIELDS, text);
		this.text = text;
	}
	
	@Override
	protected void clicked(int selectedId, List<Integer> customers) {
		show(CustomerPage.class, Integer.toString(selectedId));
	}
	@Override
	public String getTitle() {
		return "Treffer für " + text;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}
	
}
