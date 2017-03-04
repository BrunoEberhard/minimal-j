package org.minimalj.example.miniboost.frontend;

import static org.minimalj.example.miniboost.model.Customer.$;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.model.Customer;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.repository.query.By;

public class CustomerSearchPage extends SearchPage<Customer> {

	private static final Object[] keys = {$.matchcode, $.name1, $.name2, $.address.getCountryAndZip(), $.address.city, $.address.street};
	
	public CustomerSearchPage(String query) {
		super(query, keys);
	}

	@Override
	protected List<Customer> load(String query, Object[] sortKey, boolean[] sortDirection, int offset, int rows) {
		return Backend.find(Customer.class, By.search(query).limit(offset, rows));
	}

	@Override
	public ObjectPage<Customer> createDetailPage(Customer owner) {
		return null; // no detail
	}

}
