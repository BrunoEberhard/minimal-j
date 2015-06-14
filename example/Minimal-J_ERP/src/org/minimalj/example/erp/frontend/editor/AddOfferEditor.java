package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.OfferForm;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.example.erp.model.CustomerView;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.ViewUtil;

public class AddOfferEditor extends Editor<Offer, Void> {

	private final Customer customer;
	
	public AddOfferEditor(Customer customer) {
		this.customer = customer;
	}
	
	@Override
	public Form<Offer> createForm() {
		return new OfferForm(true);
	}
	
	@Override
	protected Offer createObject() {
		Customer startWithCustomer = customer;

		Offer offer = new Offer();
		if (startWithCustomer != null) {
			offer.customer = ViewUtil.view(startWithCustomer, new CustomerView());
		}
		return offer;
	}

	@Override
	public Void save(Offer offer) {
		Backend.getInstance().insert(offer);
		return null;
	}

	@Override
	public String getTitle() {
		return "Offerte hinzufügen";
	}

}
