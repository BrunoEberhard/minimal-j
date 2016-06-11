package org.minimalj.example.miniboost.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.model.Customer;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddCustomerEditor extends NewObjectEditor<Customer> {

	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(Form.EDITABLE);
	}

	@Override
	protected Customer save(Customer owner) {
		return Backend.save(owner);
	}
	
	@Override
	protected void finished(Customer newCustomer) {
		Frontend.show(new CustomerTablePage());
	}

}
