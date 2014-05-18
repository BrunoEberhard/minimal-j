package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Customer.*;

import java.util.List;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;
import ch.openech.mj.util.IdUtils;


public class CustomerTablePage extends TablePage<Customer> implements RefreshablePage {

	private final String text;
	
	public static final Object[] FIELDS = {
		CUSTOMER.firstName, //
		CUSTOMER.name, //
		CUSTOMER.birthDay, //
	};
	
	public CustomerTablePage(PageContext context, String text) {
		super(context, FIELDS, text);
		this.text = text;
	}
	
	@Override
	protected List<Customer> load(String query) {
		return Backend.getInstance().search(Customer.class, query, 100);
	}

	@Override
	protected void clicked(Customer selectedObject, List<Customer> selectedObjects) {
		show(CustomerPage.class, IdUtils.getIdString(selectedObject));
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
