package org.minimalj.example.library.frontend.form;

import static org.minimalj.example.library.model.Customer.*;

import org.fluttercode.datafactory.impl.DataFactory;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.form.Form;

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
		DataFactory df = new DataFactory();
		customer.name = df.getLastName();
		customer.firstName = df.getFirstName();
	}
	
}
