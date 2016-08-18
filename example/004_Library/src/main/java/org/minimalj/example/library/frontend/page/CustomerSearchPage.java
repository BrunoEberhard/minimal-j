package org.minimalj.example.library.frontend.page;

import static org.minimalj.example.library.model.Customer.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage.SimpleSearchPage;
import org.minimalj.persistence.criteria.By;


public class CustomerSearchPage extends SimpleSearchPage<Customer> {

	public static final Object[] FIELDS = {
		$.firstName, //
		$.name, //
		$.dateOfBirth, //
	};
	
	public CustomerSearchPage(String query) {
		super(query, FIELDS);
	}
	
	@Override
	protected List<Customer> load(String query) {
		return Backend.read(Customer.class, By.search(query), 100);
	}

	@Override
	public ObjectPage<Customer> createDetailPage(Customer customer) {
		return new CustomerPage(customer);
	}
	
}
