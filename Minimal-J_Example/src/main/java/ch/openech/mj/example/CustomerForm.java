package ch.openech.mj.example;

import static ch.openech.mj.example.model.Customer.*;
import ch.openech.mj.autofill.NameGenerator;
import ch.openech.mj.edit.form.Form;
import ch.openech.mj.example.model.Customer;

public class CustomerForm extends Form<Customer> {

	public CustomerForm(boolean editable) {
		super(editable);
		
		line(CUSTOMER.firstName);
		line(CUSTOMER.name);
		line(CUSTOMER.birthDay);
		line(CUSTOMER.remarks);
		
	}

	@Override
	protected void fillWithDemoData(Customer customer) {
		super.fillWithDemoData(customer);
		customer.name = NameGenerator.officialName();
		customer.firstName = "Dejan";
		// customer.firstName = FirstNameGenerator.getFirstName(Math.random() < 0.5);
	}
	
}
