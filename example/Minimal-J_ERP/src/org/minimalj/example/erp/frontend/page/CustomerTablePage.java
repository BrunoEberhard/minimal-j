package org.minimalj.example.erp.frontend.page;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.transaction.criteria.Criteria;


public class CustomerTablePage extends TablePage<Customer> {

	public CustomerTablePage() {
		super(CustomerSearchPage.FIELDS);
	}
	
	@Override
	protected List<Customer> load() {
		return Backend.getInstance().read(Customer.class, Criteria.all(), 100);
	}

	@Override
	public void action(Customer selectedObject, List<Customer> selectedObjects) {
		ClientToolkit.getToolkit().show(new CustomerPage(selectedObject));
	}

}
