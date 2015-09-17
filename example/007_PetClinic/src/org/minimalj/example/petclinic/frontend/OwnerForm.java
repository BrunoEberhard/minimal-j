package org.minimalj.example.petclinic.frontend;

import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.frontend.form.Form;

public class OwnerForm extends Form<Owner> {

	public static final boolean SHOW_PETS = true;
	
	public OwnerForm(boolean editable, boolean showPets) {
		super(editable);
		
		line(Owner.$.person.firstName);
		line(Owner.$.person.lastName);
		line(Owner.$.address);
		line(Owner.$.city);
		line(Owner.$.telephone);
		
		if (showPets) {
			line(new PetListFormElement(Owner.$.getPets()));
		}
	}

}
