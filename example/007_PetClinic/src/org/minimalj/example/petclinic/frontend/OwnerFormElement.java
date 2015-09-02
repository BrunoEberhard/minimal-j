package org.minimalj.example.petclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.SearchDialogAction;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.ObjectFormElement;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.transaction.criteria.Criteria;

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
	protected void show(Owner placeOfOrigin) {
		add(placeOfOrigin, new RemoveObjectAction());
	}

	@Override
	protected Action[] getActions() {
		return new Action[] { new OwnerAction() };
	}

	public class OwnerAction extends SearchDialogAction<Owner> {
		
		public OwnerAction() {
			super(new Object[]{Owner.$.person.lastName});
		}

		@Override
		protected void save(Owner object) {
			Owner person = Backend.persistence().read(Owner.class, object.id);
			setValue(person);
		}

		@Override
		public List<Owner> search(String searchText) {
			return Backend.persistence().read(Owner.class, Criteria.search(searchText), 100);
		}

	}
}
