package ch.openech.mj.example;

import java.util.List;

import ch.openech.mj.edit.SearchDialogAction;
import ch.openech.mj.edit.fields.ObjectFlowField;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;

public class CustomerField extends ObjectFlowField<Customer> {

	public CustomerField(PropertyInterface property) {
		super(property);
	}
	
	public CustomerField(Customer key) {
		this(Keys.getProperty(key));
	}
	
	@Override
	public IForm<Customer> createFormPanel() {
		// not used
		return null;
	}

	@Override
	protected void show(Customer customer) {
		addHtml(customer.firstName + " " + customer.name);
	}

	@Override
	protected void showActions() {
        addAction(new CustomerSearchAction());
        addAction(new RemoveObjectAction());
	}
	
	public class CustomerSearchAction extends SearchDialogAction<Customer> {
		
		public CustomerSearchAction() {
			super(Customer.CUSTOMER.firstName, Customer.CUSTOMER.name);
		}

		@Override
		protected List<Customer> search(String text) {		
			List<Customer> resultList = ExamplePersistence.getInstance().customer().find(text);
			return resultList;
		}

		@Override
		protected void save(Customer object) {
			setObject(object);
		}

	}
}
