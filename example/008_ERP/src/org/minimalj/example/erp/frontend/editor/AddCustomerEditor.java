package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.CustomerForm;
import org.minimalj.example.erp.frontend.page.CustomerPage;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddCustomerEditor extends NewObjectEditor<Customer> {

	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(true);
	}

	@Override
	protected Object save(Customer customer) {
		return Backend.insert(customer);
	}

    @Override
    protected void finished(Object newId) {
    	Frontend.show(new CustomerPage(newId));
    }

}
