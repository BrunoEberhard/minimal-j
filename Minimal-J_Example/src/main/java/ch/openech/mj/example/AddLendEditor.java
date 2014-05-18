package ch.openech.mj.example;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.Lend;

public class AddLendEditor extends Editor<Lend> {

	private Customer startWithCustomer;
	
	public AddLendEditor() {
		// empty
	}
	
	public AddLendEditor(Customer customer) {
		this.startWithCustomer = customer;
	}
	
	@Override
	public IForm<Lend> createForm() {
		return new LendForm(true);
	}
	
	@Override
	protected Lend newInstance() {
		Lend lend = new Lend();
		lend.customer = startWithCustomer;
		return lend;
	}

	@Override
	public String save(Lend lend) throws Exception {
		Backend.getInstance().insert(lend);
		return "";
	}

	@Override
	public String getTitle() {
		return "Ausleihe hinzufügen";
	}

}
