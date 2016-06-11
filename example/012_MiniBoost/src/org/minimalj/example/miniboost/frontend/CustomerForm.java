package org.minimalj.example.miniboost.frontend;

import static org.minimalj.example.miniboost.model.Customer.*;

import org.minimalj.example.miniboost.model.Customer;
import org.minimalj.frontend.form.Form;

public class CustomerForm extends Form<Customer> {

	public CustomerForm(boolean editable) {
		super(editable, 2);
		
		text("Name und Anschrift");
		line($.matchcode, $.debtNo);
		line($.name1, $.name2);
		line($.name3);
		line($.address.country, $.address.zip);
		line($.address.city, $.address.street);
		
		text("Kontaktdaten");
		line($.contact.phone1, $.contact.phone2);
		line($.contact.fax, $.contact.email);
	}

}
