package ch.openech.mj.example;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.page.CustomerViewPage;
import ch.openech.mj.page.Page;

public class AddCustomerEditor extends Editor<Customer> {

	@Override
	public IForm<Customer> createForm() {
		return new CustomerForm(true);
	}
	
	@Override
	public boolean save(Customer customer) throws Exception {
		int id = ExamplePersistence.getInstance().customer().insert(customer);
		setFollowLink(Page.link(CustomerViewPage.class, Integer.toString(id)));
		return true;
	}

	@Override
	public String getTitle() {
		return "Kunde hinzufügen";
	}

}
