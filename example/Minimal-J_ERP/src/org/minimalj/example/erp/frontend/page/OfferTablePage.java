package org.minimalj.example.erp.frontend.page;

import static org.minimalj.example.erp.model.Offer.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;


public class OfferTablePage extends TablePage<Offer> {

	private Customer customer;
	
	public static final Object[] FIELDS = {
		$.offerNr, //
		$.title, //
		$.creationdate, //
		$.validuntil, //
		$.customer.surname, //
		$.totalPrice, //
		$.tax_in_percent, //
		$.discount_in_percent, //
		$.grosstotalprice, //
		$.fixprice, //
		$.isordered, //
	};
	
	public OfferTablePage(Customer customer) {
		super(FIELDS);
		this.customer = customer;
	}
	
	@Override
	protected List<Offer> load() {
		return Backend.getInstance().read(Offer.class, Criteria.equals(Offer.$.customer, customer), 100);
	}

	@Override
	public String getTitle() {
		return "Offers for " + customer.surname;
	}

}
