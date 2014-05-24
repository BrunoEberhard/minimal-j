package org.minimalj.example.library.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.frontend.editor.AddLendEditor;
import org.minimalj.example.library.frontend.form.CustomerForm;
import org.minimalj.example.library.model.Customer;
import org.minimalj.frontend.edit.form.IForm;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectViewPage;
import org.minimalj.frontend.page.PageContext;
import org.minimalj.frontend.page.PageLink;
import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.frontend.toolkit.ResourceAction;
import org.minimalj.util.IdUtils;

public class CustomerPage extends ObjectViewPage<Customer> {

	private final Customer customer;

	public CustomerPage(PageContext context, String id) {
		super(context);
		customer = lookup(id);
	}
	
	private static Customer lookup(String id) {
		return Backend.getInstance().read(Customer.class, Long.valueOf(id));
	}

	@Override
	protected IForm<Customer> createForm() {
		return new CustomerForm(false);
	}

	@Override
	public String getTitle() {
		return customer.display();
	}

	@Override
	public ActionGroup getMenu() {
		ActionGroup menu = new ActionGroup("Person");
		menu.add(new AddLendEditor(customer));
		menu.add(new ShowLendsofCustomerAction(customer));
		return menu;
	}

	@Override
	protected Customer getObject() {
		return customer;
	}
	
	private static class ShowLendsofCustomerAction extends ResourceAction {

		private final Customer customer;
		
		public ShowLendsofCustomerAction(Customer customer) {
			this.customer = customer;
		}

		@Override
		public void action(IComponent context) {
			((PageContext) context).show(PageLink.link(LendTablePage.class, IdUtils.getIdString(customer)));
		}
		
	}
	
	
}
