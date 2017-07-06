package org.minimalj.example.minimalclinic.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Owner;
import org.minimalj.example.minimalclinic.model.Pet;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ReferenceFormElement;

public class AddPetEditor extends NewObjectEditor<Pet> {

	@Override
	protected Form<Pet> createForm() {
		Form<Pet> form = new Form<>();
		form.line(new ReferenceFormElement<>(Pet.$.owner, Owner.$.person.firstName, Owner.$.person.lastName));
		form.line(Pet.$.type);
		form.line(Pet.$.name);
		form.line(Pet.$.birthDate);
		return form;
	}

	@Override
	protected Pet save(Pet pet) {
		return Backend.save(pet);
	}
	
	@Override
	protected void finished(Pet newPet) {
		Frontend.show(new PetTablePage());
	}

}
