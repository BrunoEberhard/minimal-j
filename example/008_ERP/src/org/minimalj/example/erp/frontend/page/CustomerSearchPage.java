package org.minimalj.example.erp.frontend.page;

import static org.minimalj.example.erp.model.Customer.$;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage.SimpleSearchPage;
import org.minimalj.repository.criteria.By;


public class CustomerSearchPage extends SimpleSearchPage<Customer> {

	public static final Object[] FIELDS = {
		$.company, //
		$.firstname, //
		$.surname, //
		$.email, //
		$.address, //
		$.zip, //
		$.city, //
		$.customersince, //
		$.title, //
		$.salutation, //
		$.customerNr, //
	};
	
	public CustomerSearchPage(String query) {
		super(query, FIELDS);
	}
	
	@Override
	protected List<Customer> load(String query) {
		return Backend.read(Customer.class, By.search(query), 100);
	}

	@Override
	public ObjectPage<Customer> createDetailPage(Customer initialObject) {
		return new CustomerPage(initialObject);
	}

}
