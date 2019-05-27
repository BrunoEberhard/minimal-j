package org.minimalj.example.erp.frontend.page;

import static org.minimalj.example.erp.model.Offer.$;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.TableDetailPage;
import org.minimalj.repository.query.By;

public class OfferTablePage extends TableDetailPage<Offer> {

	private Customer customer;
	
	public static final Object[] FIELDS = {
		$.offerNr, //
		$.title, //
		$.creationdate, //
		$.validuntil, //
		$.customer.surname, //
		$.totalPrice, //
		$.taxInPercent, //
		$.discountInPercent, //
		$.grosstotalprice, //
		$.fixprice, //
		$.isordered, //
	};
	
	public OfferTablePage(Customer customer) {
		this.customer = customer;
	}

	@Override
	protected Object[] getColumns() {
		return FIELDS;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
		refresh();
	}
	
	@Override
	protected List<Offer> load() {
		return Backend.find(Offer.class, By.field(Offer.$.customer, customer).limit(100));
	}

	@Override
	public String getTitle() {
		return "Offers for " + customer.surname;
	}

	@Override
	protected ObjectPage<Offer> getDetailPage(Offer offer) {
		return new OfferPage(offer);
	}
	
}
