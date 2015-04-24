package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.CustomerForm;
import org.minimalj.example.erp.frontend.page.CustomerPage;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.Page;

public class AddCustomerEditor extends Editor<Customer> {

	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(true);
	}

	@Override
	protected Page save(Customer customer) throws Exception {
		Customer savedCustomer = Backend.getInstance().insert(customer);
		return new CustomerPage(savedCustomer);
	}

}
