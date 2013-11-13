package ch.openech.mj.example;

import static ch.openech.mj.example.model.Customer.*;
import ch.openech.mj.edit.form.Form;
import ch.openech.mj.example.model.Customer;

public class CustomerForm extends Form<Customer> {

	public CustomerForm(boolean editable) {
		super(editable);
		
		line(CUSTOMER.customerIdentification.firstName);
		line(CUSTOMER.customerIdentification.name);
		line(CUSTOMER.customerIdentification.birthDay);
		line(CUSTOMER.remarks);
		
	}
	
}
