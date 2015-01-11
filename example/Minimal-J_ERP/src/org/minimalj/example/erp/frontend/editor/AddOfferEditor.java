package org.minimalj.example.erp.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.form.OfferForm;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.example.erp.model.CustomerView;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.model.ViewUtil;

public class AddOfferEditor extends Editor<Offer> {

	private Customer startWithCustomer;
	
	public AddOfferEditor() {
		// empty
	}
	
	public AddOfferEditor(Customer customer) {
		this.startWithCustomer = customer;
	}
	
	@Override
	public Form<Offer> createForm() {
		return new OfferForm(true);
	}
	
	@Override
	protected Offer newInstance() {
		Offer offer = new Offer();
		if (startWithCustomer != null) {
			offer.customer = ViewUtil.view(startWithCustomer, new CustomerView());
		}
		return offer;
	}

	@Override
	public String save(Offer offer) throws Exception {
		Backend.getInstance().insert(offer);
		return "";
	}

	@Override
	public String getTitle() {
		return "Offerte hinzufügen";
	}

}
