package org.minimalj.example.minimalclinic.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Owner;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddOwnerEditor extends NewObjectEditor<Owner> {

	@Override
	protected Form<Owner> createForm() {
		return new OwnerForm(true);
	}

	@Override
	protected Owner save(Owner owner) {
		return Backend.save(owner);
	}
	
	@Override
	protected void finished(Owner newOwner) {
		Frontend.show(new OwnerPage(newOwner));
	}

}
