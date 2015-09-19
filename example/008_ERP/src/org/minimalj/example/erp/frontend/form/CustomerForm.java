package org.minimalj.example.erp.frontend.form;

import static org.minimalj.example.erp.model.Customer.*;

import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.form.Form;

public class CustomerForm extends Form<Customer> {

	public CustomerForm(boolean editable) {
		super(editable, 2);
		
		line($.customerNr);
		line($.salutation, $.title);
		line($.firstname);
		line($.surname);
		line($.email);
		line($.company);
		line($.address);
		line($.zip, $.city);
		line($.customersince);
	}
}
