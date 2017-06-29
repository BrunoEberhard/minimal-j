package org.minimalj.example.minimalclinic.frontend;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Vet;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.util.resources.Resources;

public class VetPage extends ObjectPage<Vet> {

	public VetPage(Vet object) {
		super(object);
	}
	
	public VetPage(Object objectId) {
		super(Vet.class, objectId);
	}
	
	@Override
	public String getTitle() {
		return Resources.getString(Vet.class) + ": " + getObject().person.getName();
	}
	
	@Override
	protected Form<Vet> createForm() {
		return new VetForm(Form.READ_ONLY);
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<>();
		actions.add(new VetEditor());
		return actions;
	}
	
	public class VetEditor extends ObjectEditor {

		@Override
		protected Form<Vet> createForm() {
			return new VetForm(Form.EDITABLE);
		}

		@Override
		protected Vet save(Vet vet) {
			return Backend.save(vet);
		}
	}
}
