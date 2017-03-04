package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Customer.$;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.repository.query.By;


public class CustomerSearchPage extends SearchPage<Customer> {

	public static final Object[] FIELDS = {
		$.firstName, //
		$.name, //
		$.dateOfBirth, //
	};
	
	public CustomerSearchPage(String query) {
		super(query, FIELDS);
	}
	
	@Override
	protected List<Customer> load(String query, Object[] sortKey, boolean[] sortDirection, int offset, int rows) {
		return Backend.find(Customer.class, By.search(query).limit(offset, rows));
	}

	@Override
	public ObjectPage<Customer> createDetailPage(Customer customer) {
		return new CustomerPage(customer);
	}
	
}
