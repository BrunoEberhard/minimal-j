package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.CustomerForm;
import org.minimalj.example.erp.frontend.page.CustomerPage;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.PageLink;

public class AddCustomerEditor extends Editor<Customer> {

	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(true);
	}

	@Override
	protected String save(Customer customer) throws Exception {
		Object id = Backend.getInstance().insert(customer);
		return PageLink.link(CustomerPage.class, id.toString());
	}

}
