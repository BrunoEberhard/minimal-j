package org.minimalj.example.library.frontend.page;

import java.util.List;

import org.minimalj.example.library.frontend.editor.AddLendEditor;
import org.minimalj.example.library.frontend.form.CustomerForm;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.DetailPageAction;
import org.minimalj.frontend.page.ObjectPage;

public class CustomerPage extends ObjectPage<Customer> {

	public CustomerPage(Customer customer) {
		super(customer);
	}
	
	public CustomerPage(Object id) {
		super(Customer.class, id);
	}
	
	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(false);
	}

	@Override
	public List<Action> getActions() {
		Customer customer = getObject();
		ActionGroup menu = new ActionGroup("Customer");
		menu.add(new AddLendEditor(customer));
		menu.add(new DetailPageAction(this, new LendTablePage(customer)));
		return menu.getItems();
	}
	
}
