package ch.openech.mj.example;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.page.CustomerPage;
import ch.openech.mj.page.PageLink;

public class AddCustomerEditor extends Editor<Customer> {

	@Override
	public IForm<Customer> createForm() {
		return new CustomerForm(true);
	}
	
	@Override
	public String save(Customer customer) throws Exception {
		int id = ExamplePersistence.getInstance().customer.insert(customer);
		ExamplePersistence.getInstance().commit();
		return PageLink.link(CustomerPage.class, Integer.toString(id));
	}

	@Override
	public String getTitle() {
		return "Kunde hinzufügen";
	}

}
