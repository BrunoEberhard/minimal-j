package org.minimalj.example.petclinic.frontend;

import java.util.Arrays;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.example.petclinic.model.Pet;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.util.resources.Resources;

public class OwnerPage extends ObjectPage<Owner> {

	
	public OwnerPage(Owner object) {
		super(object);
	}
	
	public OwnerPage(Object objectId) {
		super(Owner.class, objectId);
	}
	
	@Override
	public String getTitle() {
		return Resources.getString(Owner.class) + ": " + getObject().render(RenderType.PLAIN_TEXT);
	}
	
	@Override
	protected Form<Owner> createForm() {
		Form<Owner> form = new Form<>(Form.READ_ONLY);
		form.line(Owner.$.person.firstName);
		form.line(Owner.$.person.lastName);
		form.line(Owner.$.address);
		form.line(Owner.$.city);
		form.line(Owner.$.telephone);
		form.line(new PetListFormElement(Owner.$.getPets()));
		return form;
	}

	@Override
	public List<Action> getActions() {
		return Arrays.asList(new OwnerEditor(), new AddPetEditor() /*, new DetailPageAction(petTablePage) */);
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
