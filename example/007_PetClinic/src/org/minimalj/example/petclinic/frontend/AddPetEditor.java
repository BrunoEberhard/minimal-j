package org.minimalj.example.petclinic.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.example.petclinic.model.Pet;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddPetEditor extends NewObjectEditor<Pet> {

	private final Owner defaultOwner;
	
	public AddPetEditor() {
		defaultOwner = null;
	}

	public AddPetEditor(Owner owner) {
		defaultOwner = owner;
	}
	
	@Override
	protected Pet createObject() {
		Pet pet = super.createObject();
		pet.owner = defaultOwner;
		return pet;
	}
	
	@Override
	protected Form<Pet> createForm() {
		Form<Pet> form = new Form<>();
		form.line(Pet.$.type);
		form.line(Pet.$.name);
		form.line(Pet.$.birthDate);
		form.line(new OwnerFormElement(Pet.$.owner));
		return form;
	}

	@Override
	protected Object save(Pet pet) {
		return Backend.persistence().insert(pet);
	}

}
