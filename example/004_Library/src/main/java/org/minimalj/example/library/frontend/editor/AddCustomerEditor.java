package org.minimalj.example.library.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.frontend.form.CustomerForm;
import org.minimalj.example.library.frontend.page.CustomerPage;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class AddCustomerEditor extends NewObjectEditor<Customer> {

	@Override
	public Form<Customer> createForm() {
		return new CustomerForm(true);
	}
	
	@Override
	protected Customer save(Customer customer) {
		return Backend.getInstance().insert(customer);
	}

	@Override
	public String getTitle() {
		return "Kunde hinzufügen";
	}
	
	@Override
	protected void finished(Customer result) {
		ClientToolkit.getToolkit().show(new CustomerPage(result));
	}

}
