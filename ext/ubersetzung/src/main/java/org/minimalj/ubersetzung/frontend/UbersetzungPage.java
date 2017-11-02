package org.minimalj.ubersetzung.frontend;

import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.ubersetzung.model.Ubersetzung;

public class UbersetzungPage extends ObjectPage<Ubersetzung> {

	public UbersetzungPage(Ubersetzung object) {
		super(object);
	}

	@Override
	protected Form<Ubersetzung> createForm()  {
		Form<Ubersetzung> form = new Form<>();
		form.line(Ubersetzung.$.lang);
		form.line(Ubersetzung.$.country);
		return form;
	}

}
