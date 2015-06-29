package org.minimalj.example.erp.frontend.page;

import org.minimalj.example.erp.frontend.form.OfferForm;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ObjectPage;

public class OfferPage extends ObjectPage<Offer> {

	public OfferPage(Offer offer) {
		super(offer);
	}

	@Override
	protected Form<Offer> createForm() {
		return new OfferForm(false);
	}

}
