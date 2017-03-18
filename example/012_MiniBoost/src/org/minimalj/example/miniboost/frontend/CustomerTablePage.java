package org.minimalj.example.miniboost.frontend;

import static org.minimalj.example.miniboost.model.Customer.$;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.model.Customer;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.repository.query.By; 

public class CustomerTablePage extends TablePage<Customer> {

	private static final Object[] keys = {$.matchcode, $.name1, $.name2, $.address.country, $.address.zip, $.address.city, $.address.street};
	
	public CustomerTablePage() {
		super(keys);
	}

	@Override
	protected List<Customer> load() {
		return Backend.find(Customer.class, By.limit(100));
	}

}
