package org.minimalj.example.petclinic.frontend;

import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ObjectFormElement;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

public class OwnerFormElement extends ObjectFormElement<Owner> {

	public OwnerFormElement(PropertyInterface property) {
		super(property);
	}
	
	public OwnerFormElement(Owner key) {
		this(Keys.getProperty(key));
	}
	
	@Override
	public Form<Owner> createFormPanel() {
		// not used
		return null;
	}

	@Override
	protected void show(Owner owner) {
		add(owner);
	}

}
