package org.minimalj.example.library.frontend.form;

import static org.minimalj.example.library.model.Customer.*;

import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.util.mock.MockPrename;
import org.minimalj.util.mock.MockName;

public class CustomerForm extends Form<Customer> {

	public CustomerForm(boolean editable) {
		super(editable);
		
		line($.firstName);
		line($.name);
		line($.dateOfBirth);
		line($.remarks);
		
	}

	@Override
	protected void fillWithDemoData(Customer customer) {
		super.fillWithDemoData(customer);
		customer.name = MockName.officialName();
		customer.firstName = MockPrename.getFirstName(Math.random() < 0.5);
	}
	
}
