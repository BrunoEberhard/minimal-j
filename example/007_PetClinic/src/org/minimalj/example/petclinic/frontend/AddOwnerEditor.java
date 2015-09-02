package org.minimalj.example.petclinic.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddOwnerEditor extends NewObjectEditor<Owner> {

	@Override
	protected Form<Owner> createForm() {
		Form<Owner> form = new Form<>();
		form.line(Owner.$.person.firstName);
		form.line(Owner.$.person.lastName);
		form.line(Owner.$.address);
		form.line(Owner.$.city);
		form.line(Owner.$.telephone);
		return form;
	}

	@Override
	protected Object save(Owner owner) {
		return Backend.persistence().insert(owner);
	}

}
