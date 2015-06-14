package org.minimalj.example.erp.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.editor.AddOfferEditor;
import org.minimalj.example.erp.frontend.form.CustomerForm;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class CustomerPage extends ObjectPage<Customer> {

	public CustomerPage(Customer customer) {
		super(customer);
	}
	
	@Override
	public ActionGroup getMenu() {
		ActionGroup menu = new ActionGroup("Customer");
		menu.add(new AddOfferEditor(getObject()));
		menu.add(new ShowOffersAction());
		menu.addSeparator();
		menu.add(new CustomerEditor());
		return menu;
	}

	@Override
	protected Form<Customer> createForm() {
		return new CustomerForm(false);
	}

	private class ShowOffersAction extends Action {

		@Override
		public void action() {
			ClientToolkit.getToolkit().show(new OfferTablePage(getObject()), false);
		}
	}

	public class CustomerEditor extends SimpleEditor<Customer> {

		@Override
		protected Customer createObject() {
			return getObject();
		}	
	
		@Override
		protected Form<Customer> createForm() {
			return new CustomerForm(true);
		}

		@Override
		protected Customer save(Customer customer) {
			return Backend.getInstance().update(customer);
		}

		@Override
		protected void finished(Customer result) {
			setObject(result);
		}
	}

}
