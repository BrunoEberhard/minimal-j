package org.minimalj.example.erp.frontend.page;

import static org.minimalj.example.erp.model.Offer.*;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;


public class OfferTablePage extends TablePage<Offer> {

	private final String text;
	
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
	
	public OfferTablePage(String text) {
		super(FIELDS, text);
		this.text = text;
	}
	
	@Override
	protected List<Offer> load(String text) {
		return Backend.getInstance().read(Offer.class, Criteria.equals(Offer.$.customer.id, text), 100);
	}

	@Override
	protected void clicked(Offer selectedObject, List<Offer> selectedObjects) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getTitle() {
		return text;
	}

}
