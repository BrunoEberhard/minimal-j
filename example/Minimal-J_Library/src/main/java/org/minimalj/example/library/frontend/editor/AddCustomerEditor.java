package org.minimalj.example.library.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.frontend.form.CustomerForm;
import org.minimalj.example.library.frontend.page.CustomerPage;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.Page;

public class AddCustomerEditor extends Editor<Customer> {

	@Override
	public Form<Customer> createForm() {
		return new CustomerForm(true);
	}
	
	@Override
	protected Page save(Customer customer) throws Exception {
		Customer savedCustomer = Backend.getInstance().insert(customer);
		return new CustomerPage(savedCustomer);
	}

	@Override
	public String getTitle() {
		return "Kunde hinzufügen";
	}

}
