package org.minimalj.example.erp.frontend.page;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.TableDetailPage;
import org.minimalj.repository.query.By;


public class CustomerTablePage extends TableDetailPage<Customer> {

	@Override
	protected Object[] getColumns() {
		return CustomerSearchPage.FIELDS;
	}
	
	@Override
	protected List<Customer> load() {
		return Backend.find(Customer.class, By.limit(100));
	}

	@Override
	protected ObjectPage<Customer> getDetailPage(Customer customer) {
		return new CustomerPage(customer);
	}

}
