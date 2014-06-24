package org.minimalj.example.library.frontend.form;

import static org.minimalj.example.library.model.Customer.*;

import org.minimalj.autofill.FirstNameGenerator;
import org.minimalj.autofill.NameGenerator;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.edit.form.Form;

public class CustomerForm extends Form<Customer> {

	public CustomerForm(boolean editable) {
		super(editable);
		
		line(CUSTOMER.firstName);
		line(CUSTOMER.name);
		line(CUSTOMER.dateOfBirth);
		line(CUSTOMER.remarks);
		
	}

	@Override
	protected void fillWithDemoData(Customer customer) {
		super.fillWithDemoData(customer);
		customer.name = NameGenerator.officialName();
		customer.firstName = FirstNameGenerator.getFirstName(Math.random() < 0.5);
	}
	
}
