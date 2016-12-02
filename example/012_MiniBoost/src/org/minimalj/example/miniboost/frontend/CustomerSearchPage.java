package org.minimalj.example.miniboost.frontend;

import static org.minimalj.example.miniboost.model.Customer.$;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.model.Customer;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage.SimpleSearchPage;
import org.minimalj.persistence.criteria.By;

public class CustomerSearchPage extends SimpleSearchPage<Customer> {

	private static final Object[] keys = {$.matchcode, $.name1, $.name2, $.address.getCountryAndZip(), $.address.city, $.address.street};
	
	public CustomerSearchPage(String query) {
		super(query, keys);
	}

	@Override
	protected List<Customer> load(String query) {
		return Backend.read(Customer.class, By.search(query), 100);
	}

	@Override
	public ObjectPage<Customer> createDetailPage(Customer owner) {
		return null; // no detail
	}

}
