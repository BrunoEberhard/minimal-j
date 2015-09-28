package org.minimalj.example.erp.frontend.page;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.TablePage.SimpleTablePageWithDetail;
import org.minimalj.transaction.predicate.By;


public class CustomerTablePage extends SimpleTablePageWithDetail<Customer> {

	public CustomerTablePage() {
		super(CustomerSearchPage.FIELDS);
	}
	
	@Override
	protected List<Customer> load() {
		return Backend.persistence().read(Customer.class, By.all(), 100);
	}

	@Override
	protected ObjectPage<Customer> createDetailPage(Customer customer) {
		return new CustomerPage(customer);
	}

}
