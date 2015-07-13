package org.minimalj.example.library.frontend.page;

import java.util.List;

import org.minimalj.example.library.frontend.editor.AddLendAction;
import org.minimalj.example.library.frontend.form.CustomerForm;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.toolkit.Action;

public class CustomerPage extends ObjectPage<Customer> {

	public CustomerPage(Customer customer) {
		super(customer);
	}
	
	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(false);
	}

	@Override
	public String getTitle() {
		return getObject().toString();
	}

	@Override
	public List<Action> getActions() {
		Customer customer = getObject();
		ActionGroup menu = new ActionGroup("Customer");
		menu.add(new AddLendAction(customer));
		menu.add(new LendTablePage(customer));
		return menu.getItems();
	}
	
}
