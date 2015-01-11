package org.minimalj.example.erp.model;

import org.minimalj.model.Keys;
import org.minimalj.model.View;

public class OfferView implements View<Offer> {

	public static final OfferView $ = Keys.of(OfferView.class);
	
	public Object id;

	public String offer_nr;

	public String title;
	
	public String display() {
		return title;
	}
}
