package org.minimalj.example.library.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.frontend.editor.AddLendEditor;
import org.minimalj.example.library.frontend.form.CustomerForm;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.PageLink;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ResourceAction;
import org.minimalj.util.IdUtils;

public class CustomerPage extends ObjectPage<Customer> {

	private final long id;

	public CustomerPage(String id) {
		this.id = Long.valueOf(id);
	}
	
	@Override
	public Customer loadObject() {
		return Backend.getInstance().read(Customer.class, id);
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
		menu.add(new ShowLendsofCustomerAction(customer));
		return menu;
	}

	private static class ShowLendsofCustomerAction extends ResourceAction {

		private final Customer customer;
		
		public ShowLendsofCustomerAction(Customer customer) {
			this.customer = customer;
		}

		@Override
		public void action() {
			ClientToolkit.getToolkit().show(PageLink.link(LendTablePage.class, IdUtils.getIdString(customer)));
		}
		
	}
	
	
}
