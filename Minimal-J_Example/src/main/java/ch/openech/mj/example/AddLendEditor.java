package ch.openech.mj.example;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.Lend;
import ch.openech.mj.server.DbService;
import ch.openech.mj.server.Services;

public class AddLendEditor extends Editor<Lend> {

	private Customer startWithCustomer;
	
	public AddLendEditor() {
		// empty
	}
	
	public AddLendEditor(Customer customer) {
		this.startWithCustomer = null;
	}
	
	@Override
	public IForm<Lend> createForm() {
		return new LendForm(true);
	}
	
	@Override
	protected Lend newInstance() {
		Lend lend = new Lend();
		if (startWithCustomer != null) {
			lend.customer = startWithCustomer;
		}
		return lend;
	}

	@Override
	public String save(Lend lend) throws Exception {
		Services.get(DbService.class).insert(lend);
		return "";
	}

	@Override
	public String getTitle() {
		return "Ausleihe hinzufügen";
	}

}
