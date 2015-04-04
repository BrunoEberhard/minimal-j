package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.CustomerForm;
import org.minimalj.example.erp.frontend.page.CustomerPage;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.Page;

public class CustomerEditor extends Editor<Customer> {

	private final Customer customer;
	
	public CustomerEditor(Customer customer) {
		this.customer = customer;
	}
	
	@Override
	protected Customer load() {
		return Backend.getInstance().read(Customer.class, customer.id);
	}

	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(true);
	}

	@Override
	protected Page save(Customer customer) throws Exception {
		Customer savedCustomer = Backend.getInstance().update(customer);
		return new CustomerPage(savedCustomer);
	}

}
