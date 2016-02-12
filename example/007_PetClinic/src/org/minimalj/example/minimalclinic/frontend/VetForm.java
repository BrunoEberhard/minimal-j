package org.minimalj.example.minimalclinic.frontend;

import org.minimalj.example.minimalclinic.model.Vet;
import org.minimalj.frontend.form.Form;

public class VetForm extends Form<Vet> {

	public VetForm(boolean editable) {
		super(editable);
		
		line(Vet.$.person.firstName);
		line(Vet.$.person.lastName);
		line(Vet.$.specialties);
	}

}
