package org.minimalj.example.petclinic.frontend;

import java.util.Arrays;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.example.petclinic.model.Pet;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.DetailPageAction;
import org.minimalj.frontend.page.ObjectPage;

public class OwnerPage extends ObjectPage<Owner> {

	private final PetTablePage petTablePage;
	
	public OwnerPage(Owner object) {
		super(object);
		petTablePage = new PetTablePage(this);
	}
	
	public OwnerPage(Object objectId) {
		super(Owner.class, objectId);
		petTablePage = new PetTablePage(this);
	}

	@Override
	protected Form<Owner> createForm() {
		Form<Owner> form = new Form<>(Form.READ_ONLY);
		form.line(Owner.$.person.firstName);
		form.line(Owner.$.person.lastName);
		form.line(Owner.$.address);
		form.line(Owner.$.city);
		form.line(Owner.$.telephone);
		return form;
	}

	@Override
	public void setObject(Owner object) {
		super.setObject(object);
		if (Frontend.getBrowser().isDetailShown(petTablePage)) {
			petTablePage.refresh();
		}
	}
	
	@Override
	public List<Action> getActions() {
		return Arrays.asList(new OwnerEditor(), new AddPetEditor(), new DetailPageAction(petTablePage));
	}
	
	public class OwnerEditor extends ObjectEditor {

		@Override
		protected Form<Owner> createForm() {
			Form<Owner> form = new Form<>(Form.EDITABLE);
			form.line(Owner.$.person.firstName);
			form.line(Owner.$.person.lastName);
			form.line(Owner.$.address);
			form.line(Owner.$.city);
			form.line(Owner.$.telephone);
			return form;
		}

		@Override
		protected Owner save(Owner owner) {
			return Backend.persistence().update(owner);
		}
	}
	
	public class AddPetEditor extends NewObjectEditor<Pet> {
		
		@Override
		protected Pet createObject() {
			Pet pet = super.createObject();
			pet.owner = OwnerPage.this.getObject();
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
}
