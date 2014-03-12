package ch.openech.mj.example;

import ch.openech.mj.edit.SearchDialogAction;
import ch.openech.mj.edit.fields.ObjectFlowField;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.Reference;

public class CustomerField extends ObjectFlowField<Reference<Customer>> {

	public CustomerField(Reference<Customer> key) {
		super(Keys.getProperty(key));
	}
	
	@Override
	public IForm<Reference<Customer>> createFormPanel() {
		// not used
		return null;
	}

	@Override
	protected void show(Reference<Customer> customer) {
		addText(customer.get(Customer.CUSTOMER.firstName) + " " + customer.get(Customer.CUSTOMER.name));
	}

	@Override
	protected void showActions() {
        addAction(new CustomerSearchAction());
        addAction(new RemoveObjectAction());
	}
	
	public class CustomerSearchAction extends SearchDialogAction<Customer> {
		
		public CustomerSearchAction() {
			super(getComponent(), Customer.BY_FULLTEXT, Customer.CUSTOMER.firstName, Customer.CUSTOMER.name);
		}

		@Override
		protected void save(Customer object) {
			Reference<Customer> reference = getObject();
			reference.set(object);
		}

	}
}
