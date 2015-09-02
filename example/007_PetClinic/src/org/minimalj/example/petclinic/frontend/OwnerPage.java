package org.minimalj.example.petclinic.frontend;

import java.util.List;

import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ObjectPage;

public class OwnerPage extends ObjectPage<Owner> {

	public OwnerPage(Owner object) {
		super(object);
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
	public List<Action> getActions() {
		// TODO Auto-generated method stub
		return super.getActions();
	}
	
}
