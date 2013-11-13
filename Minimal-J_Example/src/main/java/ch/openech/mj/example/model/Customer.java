package ch.openech.mj.example.model;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.annotation.Size;

public class Customer {

	public static final Customer CUSTOMER = Keys.of(Customer.class);
	
	public CustomerIdentification customerIdentification = new CustomerIdentification();
	
	@Size(2000)
	public String remarks;
	
}
