package org.minimalj.example.minimalclinic.frontend;

import org.minimalj.example.minimalclinic.model.Owner;
import org.minimalj.frontend.form.Form;

public class OwnerForm extends Form<Owner> {

	public OwnerForm(boolean editable) {
		super(editable);
		
		line(Owner.$.person.firstName);
		line(Owner.$.person.lastName);
		line(Owner.$.address);
		line(Owner.$.city);
		line(Owner.$.telephone);
	}

}
