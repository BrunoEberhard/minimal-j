package ch.openech.mj.example;

import ch.openech.mj.edit.SearchDialogAction;
import ch.openech.mj.edit.fields.ObjectFlowField;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.CustomerIdentification;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.search.IndexSearch;

public class CustomerField extends ObjectFlowField<CustomerIdentification> {

	public CustomerField(PropertyInterface property) {
		super(property);
	}
	
	public CustomerField(CustomerIdentification key) {
		this(Keys.getProperty(key));
	}
	
	@Override
	public IForm<CustomerIdentification> createFormPanel() {
		// not used
		return null;
	}

	@Override
	protected void show(CustomerIdentification customer) {
		addText(customer.firstName + " " + customer.name);
	}

	@Override
	protected void showActions() {
        addAction(new CustomerSearchAction());
        addAction(new RemoveObjectAction());
	}
	
	public class CustomerSearchAction extends SearchDialogAction<Customer> {
		
		public CustomerSearchAction() {
			super(getComponent(), new IndexSearch<>(ExamplePersistence.getInstance().customerIndex), Customer.CUSTOMER.customerIdentification.firstName, Customer.CUSTOMER.customerIdentification.name);
		}

		@Override
		protected void save(Customer object) {
			setObject(object.customerIdentification);
		}

	}
}
