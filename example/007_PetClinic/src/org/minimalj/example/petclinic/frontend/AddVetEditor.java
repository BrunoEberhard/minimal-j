package org.minimalj.example.petclinic.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Vet;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddVetEditor extends NewObjectEditor<Vet> {

	@Override
	protected Form<Vet> createForm() {
		Form<Vet> form = new Form<>();
		form.line(Vet.$.person.firstName);
		form.line(Vet.$.person.lastName);
		form.line(Vet.$.specialties);
		return form;
	}

	@Override
	protected Vet save(Vet owner) {
		return Backend.save(owner);
	}
	
	@Override
	protected void finished(Vet newVet) {
		Frontend.show(new VetTablePage());
	}

}
