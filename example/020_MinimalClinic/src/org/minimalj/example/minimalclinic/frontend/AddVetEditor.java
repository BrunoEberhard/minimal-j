package org.minimalj.example.minimalclinic.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Vet;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddVetEditor extends NewObjectEditor<Vet> {

	@Override
	protected Form<Vet> createForm() {
		return new VetForm(Form.EDITABLE);
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
