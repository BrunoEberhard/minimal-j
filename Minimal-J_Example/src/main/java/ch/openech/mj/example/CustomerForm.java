package ch.openech.mj.example;

import static ch.openech.mj.example.model.Customer.CUSTOMER;
import ch.openech.mj.edit.form.Form;
import ch.openech.mj.example.model.Customer;

public class CustomerForm extends Form<Customer> {

	public CustomerForm(boolean editable) {
		super(editable);
		
		line(CUSTOMER.firstName);
		line(CUSTOMER.name);
		line(CUSTOMER.birthDay);
		
	}
	
}
