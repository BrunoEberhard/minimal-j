package org.minimalj.example.erp.frontend.page;

import org.minimalj.backend.Backend;
import org.minimalj.example.erp.frontend.editor.AddOfferEditor;
import org.minimalj.example.erp.frontend.form.CustomerForm;
import org.minimalj.example.erp.model.Customer;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageWithDetail;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;

public class CustomerPage extends ObjectPage<Customer> implements PageWithDetail {

	private OfferTablePage offerTablePage;
	
	public CustomerPage(Customer customer) {
		super(customer);
	}
	
	@Override
	public void setObject(Customer customer) {
		super.setObject(customer);
		if (offerTablePage != null) {
			offerTablePage.setCustomer(getObject());
		}
	}
	
	@Override
	public ActionGroup getMenu() {
		ActionGroup menu = new ActionGroup("Customer");
		menu.add(new AddOfferEditor(this));
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
			if (offerTablePage == null) {
				offerTablePage = new OfferTablePage(getObject());
			} else {
				offerTablePage.setCustomer(getObject());
			}
			ClientToolkit.getToolkit().show(offerTablePage, CustomerPage.this);
		}
	}

	public class CustomerEditor extends ObjectEditor {

		@Override
		protected Form<Customer> createForm() {
			return new CustomerForm(true);
		}

		@Override
		protected Customer save(Customer customer) {
			return Backend.getInstance().update(customer);
		}
	}

	@Override
	public void detailClosed(Page page) {
		if (page == offerTablePage) {
			offerTablePage = null;
		}
	}

	public void offerAdded() {
		if (offerTablePage != null) {
			offerTablePage.refresh();
		}
	}

}
