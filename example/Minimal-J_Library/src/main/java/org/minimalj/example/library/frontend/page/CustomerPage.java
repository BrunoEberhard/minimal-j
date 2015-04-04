package org.minimalj.example.library.frontend.page;

import org.minimalj.example.library.frontend.editor.AddLendEditor;
import org.minimalj.example.library.frontend.form.CustomerForm;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;

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
	public ActionGroup getMenu() {
		Customer customer = getObject();
		ActionGroup menu = new ActionGroup("Customer");
		AddLendEditor addLendEditor = new AddLendEditor(customer);
		menu.add(addLendEditor);
		menu.add(new LendTablePage(customer));
		return menu;
	}
	
}
