package ch.openech.mj.example;

import ch.openech.mj.db.Table;
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
			lend.customer = startWithCustomer.customerIdentification;
		}
		return lend;
	}

	@Override
	public String save(Lend lend) throws Exception {
		int id = ((Table<Lend>) ExamplePersistence.getInstance().getTable(Lend.class)).insert(lend);
		ExamplePersistence.getInstance().commit();
		return "";
	}

	@Override
	public String getTitle() {
		return "Ausleihe hinzufügen";
	}

}
