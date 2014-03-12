package ch.openech.mj.example;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.page.CustomerPage;
import ch.openech.mj.page.PageLink;
import ch.openech.mj.server.DbService;
import ch.openech.mj.server.Services;

public class AddCustomerEditor extends Editor<Customer> {

	@Override
	public IForm<Customer> createForm() {
		return new CustomerForm(true);
	}
	
	@Override
	public String save(Customer customer) throws Exception {
		long id = Services.get(DbService.class).insert(customer);
		return PageLink.link(CustomerPage.class, id);
	}

	@Override
	public String getTitle() {
		return "Kunde hinzufügen";
	}

}
