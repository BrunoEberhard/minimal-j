package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.CustomerForm;
import org.minimalj.example.erp.frontend.page.CustomerPage;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class AddCustomerEditor extends SimpleEditor<Customer> {

	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(true);
	}

	@Override
	protected Customer save(Customer customer) {
		return Backend.getInstance().insert(customer);
	}

    @Override
    protected void finished(Customer result) {
    	ClientToolkit.getToolkit().show(new CustomerPage(result));
    }

}
