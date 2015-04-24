package org.minimalj.example.erp.frontend.page;

import org.minimalj.example.erp.frontend.editor.AddOfferEditor;
import org.minimalj.example.erp.frontend.editor.CustomerEditor;
import org.minimalj.example.erp.frontend.form.CustomerForm;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.Action;

public class CustomerPage extends ObjectPage<Customer> {

	public CustomerPage(Customer customer) {
		super(customer);
	}
	
	@Override
	public ActionGroup getMenu() {
		ActionGroup menu = new ActionGroup("Customer");
		menu.add(new AddOfferEditor(getObject()));
		menu.add(new ShowOffersAction(getObject()));
		menu.addSeparator();
		menu.add(new CustomerEditor(getObject()));
		return menu;
	}

	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(false);
	}

	private static class ShowOffersAction extends Action {

		private final Customer customer;
		
		public ShowOffersAction(Customer customer) {
			this.customer = customer;
		}

		@Override
		public void action() {
			ClientToolkit.getToolkit().show(new OfferTablePage(customer));
		}
		
	}

}
