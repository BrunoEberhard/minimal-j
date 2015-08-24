package org.minimalj.example.library.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.frontend.form.LendForm;
import org.minimalj.example.library.model.Customer;
import org.minimalj.example.library.model.Lend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddLendAction extends NewObjectEditor<Lend> {

	private Customer startWithCustomer;
	
	public AddLendAction() {
		// empty
	}
	
	public AddLendAction(Customer customer) {
		this.startWithCustomer = customer;
	}
	
	@Override
	public Form<Lend> createForm() {
		return new LendForm(true);
	}
	
	@Override
	protected Lend createObject() {
		Lend lend = new Lend();
		lend.customer = startWithCustomer;
		return lend;
	}

	@Override
	public Object save(Lend lend) {
		return Backend.persistence().insert(lend);
	}

	@Override
	public String getTitle() {
		return "Ausleihe hinzufügen";
	}

}
