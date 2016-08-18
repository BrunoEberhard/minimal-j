package org.minimalj.example.minimalclinic.frontend;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Owner;
import org.minimalj.example.minimalclinic.model.Pet;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.DetailPageAction;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.persistence.criteria.By;
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
		return new OwnerForm(Form.READ_ONLY);
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<>();
		actions.add(new OwnerEditor());
		actions.add(new AddPetEditor());
		actions.add(new DetailPageAction(this, new PetByOwnerTablePage(getObject())));
		return actions;
	}
	
	public class OwnerEditor extends ObjectEditor {

		@Override
		protected Form<Owner> createForm() {
			return new OwnerForm(Form.EDITABLE);
		}

		@Override
		protected Owner save(Owner owner) {
			return Backend.save(owner);
		}
	}
	
	public class AddPetEditor extends NewObjectEditor<Pet> {
		
		@Override
		protected Form<Pet> createForm() {
			Form<Pet> form = new Form<>();
			form.line(Pet.$.type);
			form.line(Pet.$.name);
			form.line(Pet.$.birthDate);
			return form;
		}

		@Override
		protected Pet save(Pet pet) {
			pet.owner = OwnerPage.this.getObject();
			return Backend.save(pet);
		}
		
		@Override
		protected void finished(Pet pet) {
			OwnerPage.this.refresh();
		}
	}
	
	public static class PetByOwnerTablePage extends TablePage<Pet> {

		private static final Object[] keys = {Pet.$.name, Pet.$.type};
		
		private final Owner owner;
		
		public PetByOwnerTablePage(Owner owner) {
			super(keys);
			this.owner = owner;
		}

		@Override
		protected List<Pet> load() {
			return Backend.read(Pet.class, By.field(Pet.$.owner, owner), Integer.MAX_VALUE);
		}

	}
}
