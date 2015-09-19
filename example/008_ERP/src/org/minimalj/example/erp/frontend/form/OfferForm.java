package org.minimalj.example.erp.frontend.form;

import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.TextFormElement;

public class OfferForm extends Form<Offer> {

	public OfferForm(boolean editable) {
		super(editable);
		
		line(Offer.$.offerNr);
		line(Offer.$.title);
		line(new TextFormElement(Offer.$.customer));
		line(Offer.$.creationdate);
		line(Offer.$.validuntil);
		line(Offer.$.fixprice);
		line(new OfferArticleFormElement(Offer.$.articles, editable));
		line(Offer.$.totalPrice);
		line(Offer.$.discountInPercent);
		line(Offer.$.taxInPercent);
	}
}
