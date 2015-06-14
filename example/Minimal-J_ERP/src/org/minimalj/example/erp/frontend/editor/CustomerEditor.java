package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.CustomerForm;
import org.minimalj.example.erp.frontend.page.CustomerPage;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class CustomerEditor extends SimpleEditor<Customer> {

	private final Customer customer;
	
	public CustomerEditor(Customer customer) {
		this.customer = customer;
	}
	
	@Override
	protected Customer createObject() {
		return Backend.getInstance().read(Customer.class, customer.id);
	}

	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(true);
	}

	@Override
	protected Customer save(Customer customer) {
		return Backend.getInstance().update(customer);
	}
	
	@Override
	protected void finished(Customer result) {
		ClientToolkit.getToolkit().show(new CustomerPage(result));
	}

}
