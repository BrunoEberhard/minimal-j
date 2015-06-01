package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Customer.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.page.AbstractSearchPage;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.transaction.criteria.Criteria;


public class CustomerSearchPage extends AbstractSearchPage<Customer> {

	public static final Object[] FIELDS = {
		$.firstName, //
		$.name, //
		$.dateOfBirth, //
	};
	
	public CustomerSearchPage() {
		super(FIELDS);
	}
	
	@Override
	protected List<Customer> load(String query) {
		return Backend.getInstance().read(Customer.class, Criteria.search(query), 100);
	}

	@Override
	public void action(Customer selectedObject) {
		ClientToolkit.getToolkit().show(new CustomerPage(selectedObject));
	}
	
}
