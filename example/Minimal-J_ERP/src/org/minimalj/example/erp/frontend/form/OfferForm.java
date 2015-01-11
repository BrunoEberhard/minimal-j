package org.minimalj.example.erp.frontend.form;

import org.minimalj.example.erp.model.Customer;
import org.minimalj.example.erp.model.Offer;
import org.minimalj.frontend.edit.fields.ReferenceField;
import org.minimalj.frontend.edit.form.Form;

public class OfferForm extends Form<Offer> {

	public OfferForm(boolean editable) {
		super(editable);
		
		line(Offer.$.offerNr);
		line(Offer.$.title);
		line(new ReferenceField<>(Offer.$.customer, Customer.$.surname, Customer.$.customerNr));
		line(Offer.$.creationdate);
		line(Offer.$.validuntil);
		line(Offer.$.fixprice);
		line(new OfferArticleField(Offer.$.articles, editable));
		line(Offer.$.totalPrice);
		line(Offer.$.discount_in_percent);
		line(Offer.$.tax_in_percent);
	}
}
