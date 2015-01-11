package org.minimalj.example.erp.model;

import java.time.LocalDate;
import org.minimalj.model.annotation.Size;

public class Order {

	@Size(50)
	public String order_nr;
	
	public LocalDate orderdate;
	
	@Size(255)
	public String termsofpayment;
	
	@Size(50)
	public String billingcompany, billingaddress, billingzip, billingcity;
	
	public OfferView offer;
	public CustomerView customer;
	
	public LocalDate deliverydate;
	
	@Size(2000) // TODO Blob
	public String orderdocument;
	
	@Size(255)
	public String orderdocumentname;
	
	public boolean ispaid;
	
	public LocalDate billingdate;
	
	@Size(32)
	public String billing_nr;
}
