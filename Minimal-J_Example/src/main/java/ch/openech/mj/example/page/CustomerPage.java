package ch.openech.mj.example.page;

import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.AddLendEditor;
import ch.openech.mj.example.CustomerForm;
import ch.openech.mj.example.MjExampleApplication;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.ObjectViewPage;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.PageLink;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ResourceAction;

public class CustomerPage extends ObjectViewPage<Customer> {

	private final Customer customer;

	public CustomerPage(PageContext context, String customerId) {
		super(context);
		customer = lookup(customerId);
	}
	
	private static Customer lookup(String customerId) {
		return MjExampleApplication.persistence().read(Customer.class, Integer.valueOf(customerId));
	}

	@Override
	protected IForm<Customer> createForm() {
		return new CustomerForm(false);
	}

	@Override
	public String getTitle() {
		return customer.customerIdentification.firstName + " " + customer.customerIdentification.name;
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
			int id = MjExampleApplication.persistence().customerIdentification.getId(customer.customerIdentification);
			((PageContext) context).show(PageLink.link(LendTablePage.class, "" + id));
		}
		
	}
	
	
}
