package org.minimalj.example.petclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Pet;
import org.minimalj.example.petclinic.model.Visit;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ListFormElement;
import org.minimalj.model.Keys;

public class PetListFormElement extends ListFormElement<Pet> {

	public PetListFormElement(Object key) {
		super(Keys.getProperty(key));
	}

	@Override
	protected void showEntry(Pet pet) {
		add(pet, new AddVisitEditor(pet));
		for (Visit visit : pet.visits) {
			add(visit);
		}
	}

	@Override
	protected Form<List<Pet>> createFormPanel() {
		return null;
	}

	public class AddVisitEditor extends Editor<Visit, Pet> {

		private final Pet pet;
		
		public AddVisitEditor(Pet pet) {
			this.pet = pet;
		}

		@Override
		protected Visit createObject() {
			return new Visit();
		}

		@Override
		protected Form<Visit> createForm() {
			Form<Visit> form = new Form<Visit>();
			form.line(Visit.$.visitDate);
			form.line(Visit.$.description);
			return form;
		}

		@Override
		protected Pet save(Visit visit) {
			pet.visits.add(visit);
			return Backend.persistence().update(pet);
		}
		
		@Override
		protected void finished(Pet pet) {
			PetListFormElement.this.handleChange();
		}
	}
}
